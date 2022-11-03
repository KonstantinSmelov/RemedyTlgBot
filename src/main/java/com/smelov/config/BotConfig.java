package com.smelov.config;

import com.smelov.bot.RemedyBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
@Data
public class BotConfig {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String token;
    @Value("${bot.webhook}")
    private String webHook;

//    @Bean
//    public SetWebhook setWebhookInstance() {
//        return SetWebhook.builder().url("https://wvkeuhepgbvuyylb02vvxl.hooks.webhookrelay.com").build();
//    }
//
//    @Bean
//    public RemedyBot telegramWebhookBot(BotConfig botConfig) {
//        RemedyBot bot = new RemedyBot(botConfig);
//
//        return bot;
//    }
}
