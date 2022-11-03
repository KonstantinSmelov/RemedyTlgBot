package com.smelov.bot;

import com.smelov.config.BotConfig;
import com.smelov.handler.UpdateReceivedHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class RemedyBot extends TelegramWebhookBot {

    private final BotConfig botConfig;
    private final UpdateReceivedHandler updateReceivedHandler;

    public RemedyBot(BotConfig botConfig, @Lazy UpdateReceivedHandler updateReceivedHandler) {
        this.botConfig = botConfig;
        this.updateReceivedHandler = updateReceivedHandler;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

        updateReceivedHandler.onUpdateReceived(update);

        return null;
    }

    @Override
    public String getBotPath() {
        return botConfig.getWebHook();
    }
}
