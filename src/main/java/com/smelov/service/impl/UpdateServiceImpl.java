package com.smelov.service.impl;

import com.smelov.service.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
public class UpdateServiceImpl implements UpdateService {

    @Override
    public Long getUserId(Update update) {
        log.info("----> вход в getUserId() <----");
        Long userId = null;

        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
            log.debug("getUserId(): userId из Message: {}", userId);
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            log.debug("getUserId(): userId из CallbackQuery: {}", userId);
        }
        log.info("<---- выход из getUserId() ---->");
        return userId;
    }

    @Override
    public String getTextFromMessage(Update update) {
        log.info("----> вход в getTextFromMessage() <----");

        String msg = null;

        if (update.hasMessage()) {
            msg = update.getMessage().getText();
        }
        log.info("<---- выход из getTextFromMessage() ---->");
        return msg;
    }

    @Override
    public Long getChatId(Update update) {
        log.info("----> вход в getChatId() <----");
        Long chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            log.debug("getChatId(): chatId из Message: {}", chatId);
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            log.debug("getChatId(): chatId из CallbackQuery: {}", chatId);
        }
        log.info("<---- выход из getChatId() ---->");
        return chatId;
    }

}
