package com.smelov.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
public class UpdateServiceImpl implements com.smelov.service.UpdateService {

    @Override
    public Long getUserId(Update update) {
        Long userId = null;

        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
            log.info("getUserId(): update.getMessage().getFrom().getId(): {}", userId);
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            log.info("getUserId(): update.getCallbackQuery().getFrom().getId(): {}", userId);
        }
        return userId;
    }

    @Override
    public String getTextFromMessage(Update update) {
        String msg = null;

        if (update.hasMessage()) {
            msg = update.getMessage().getText();
        }
        return msg;
    }

    @Override
    public Long getChatId(Update update) {
        Long chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            log.info("getChatId(): update.getMessage().getChatId(): {}", chatId);
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("getChatId(): update.getCallbackQuery().getMessage().getChatId(): {}", chatId);
        }
        return chatId;
    }

}
