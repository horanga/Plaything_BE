package com.plaything.api.common.discord;

import com.plaything.api.common.discord.model.request.DiscordMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DiscordAlarm {

    @Value("${discord.alarm.enabled:false}")
    private boolean alarmEnabled;

    @Value("${discord.webhook.redis.url}")
    private String redisWebhookUrl;

    @Value("${discord.webhook.exception.url}")
    private String exceptionWebhookUrl;

    private final DiscordRedisWebhook discordRedisWebhook;

    private final DiscordExceptionWebhook discordExceptionWebhook;

    public void sendRedisAlarm(String content, String title, String description) {
        if (alarmEnabled) {
            discordRedisWebhook.sendAlarm(
                    new DiscordMessage(content,
                            List.of(new DiscordMessage.Embed(
                                    title, description
                            )))
            );
        }
    }

    public void sendServerErrorAlarm(Exception e, HttpServletRequest request) {
        if (alarmEnabled) {
            discordExceptionWebhook.sendAlarm(
                    new DiscordMessage("500 에러 발생, 원인을 파악합시다",
                            List.of(new DiscordMessage.Embed(
                                    "🚨 에러 정보 🚨", getErrorDetails(e, request)
                            )))
            );
        }
    }

    private String getErrorDetails(Exception e, HttpServletRequest request) {
        return String.format("""
                        **시간**: %s
                        **URL**: %s
                        **HTTP Method**: %s
                        
                        **에러 메시지**:
                        ```
                        %s
                        ```
                        
                        **스택 트레이스**:
                        ```java
                        %s
                        ```
                        """,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                request.getRequestURL(),
                request.getMethod(),
                e.getMessage(),
                shortenStackTrace(e)
        );
    }

    private String shortenStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        String[] lines = sw.toString().split("\n");
        int maxLines = Math.min(lines.length, 15);  // 최대 50줄

        StringBuilder stackTrace = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            stackTrace.append(lines[i]).append("\n");
        }
        return stackTrace.toString();
    }
}
