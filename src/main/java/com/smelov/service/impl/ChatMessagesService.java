package com.smelov.service.impl;

import com.smelov.StaticClass;
import com.smelov.bot.RemedyBot;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagesService {

    private final UpdateService updateService;
    private final RemedyBot remedyBot;

    @SneakyThrows
    public void deleteMessagesFromChat(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();

        for (Integer messageId : StaticClass.userMessageIds) {
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(updateService.getChatId(update));
            System.out.print("Удаляем message/callback " + messageId + "... ");
            remedyBot.execute(deleteMessage);
            System.out.println("ОК");
        }
        StaticClass.userMessageIds = new HashSet<>();
    }
}
