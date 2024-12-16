package com.plaything.api.common.discord;

import com.plaything.api.common.discord.model.request.DiscordMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "discord-error-message", url = "${discord.webhook.exception.url}")
public interface DiscordExceptionWebhook {

    @PostMapping()
    void sendAlarm(@RequestBody DiscordMessage message);
}

