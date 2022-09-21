package com.smelov.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateService {
    Long getUserId(Update update);
    String getTextFromMessage(Update update);
    Long getChatId(Update update);
}
