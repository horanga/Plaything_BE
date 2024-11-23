package com.plaything.api;


import com.plaything.api.common.discord.DiscordAlarm;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.plaything.api.common.discord.constant.MessageFormat.*;
import static com.plaything.api.common.discord.constant.MessageFormat.CLOSED_BODY;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class ApiApplication {

    private final DiscordAlarm discordAlarm;

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public RegistryEventConsumer<CircuitBreaker> myRegistryEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                        .onStateTransition(event -> {
                            log.warn("CircuitBreaker {} state changed from {} to {}",
                                    event.getCircuitBreakerName(),
                                    event.getStateTransition().getFromState(),
                                    event.getStateTransition().getToState());

                            if (event.getStateTransition().getFromState() == CircuitBreaker.State.CLOSED &&
                                    event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                                discordAlarm.sendAlarm(
                                        CIRCUIT_OPEN,
                                        OPEN_TITLE,
                                        OPEN_BODY+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            }
                            if (event.getStateTransition().getFromState() == CircuitBreaker.State.HALF_OPEN
                                    && event.getStateTransition().getToState() == CircuitBreaker.State.CLOSED) {
                                discordAlarm.sendAlarm(
                                        CIRCUIT_CLOSED,
                                        CLOSED_TITLE,
                                        CLOSED_BODY+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            }

                        }).onFailureRateExceeded(event -> log.error("CircuitBreaker {} failure rate exceeded: {}",
                                event.getCircuitBreakerName(),
                                event.getFailureRate()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {

            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {

            }
        }

                ;
    }
}

