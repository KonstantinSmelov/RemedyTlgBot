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
        log.debug("----> вход в getUserId()");
        Long userId = null;

        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
            log.trace("getUserId(): userId из Message: {}", userId);
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            log.trace("getUserId(): userId из CallbackQuery: {}", userId);
        }
        log.debug("<---- возврат из getUserId(): {}", userId);
        return userId;
    }

    @Override
    public String getTextFromMessage(Update update) {
        log.debug("----> вход в getTextFromMessage()");

        String msg = null;

        if (update.hasMessage()) {
            msg = update.getMessage().getText();
        }
        log.debug("<---- возврат из getTextFromMessage(): {}", msg);
        return msg;
    }

    @Override
    public Long getChatId(Update update) {
        log.debug("----> вход в getChatId()");
        Long chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            log.trace("getChatId(): chatId из Message: {}", chatId);
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            log.trace("getChatId(): chatId из CallbackQuery: {}", chatId);
        }
        log.debug("<---- возврат из getChatId(): {}", chatId);
        return chatId;
    }
}
