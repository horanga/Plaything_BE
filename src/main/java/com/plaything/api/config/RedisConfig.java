package com.plaything.api.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {

    SocketOptions socketOptions = SocketOptions.builder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .clientOptions(ClientOptions.builder()
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .autoReconnect(true)
            .socketOptions(socketOptions)
            .build())
        .commandTimeout(Duration.ofSeconds(5))
        .useSsl()
        .build();

    RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);

    LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
    factory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.buffered(1000));

    return factory;
  }

  @Bean
  @Primary
  public RedisTemplate<String, String> redisTemplate() {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());

    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.setEnableTransactionSupport(false);

    return redisTemplate;
  }

  @Bean
  public RedisTemplate<String, List<String>> listRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, List<String>> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));
    return template;
  }
}
