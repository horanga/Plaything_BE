package com.plaything.api.common.discord;

import com.plaything.api.common.discord.model.request.DiscordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DiscordAlarm {

    @Value("${discord.alarm.enabled:false}")
    private boolean alarmEnabled;

    private final DiscordWebhook discordWebhook;

    public void sendAlarm(String content, String title, String description) {
        if(alarmEnabled) {
            discordWebhook.sendAlarm(
                    new DiscordMessage(content,
                            List.of(new DiscordMessage.Embed(
                                    title, description
                            )))
            );
        }
    }
}
