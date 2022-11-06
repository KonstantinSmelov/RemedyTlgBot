package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.entity.Medicine;
import com.smelov.model.Status;
import com.smelov.service.TextMessageService;
import com.smelov.service.UpdateService;
import com.smelov.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagesService {

    private final RemedyBot remedyBot;
    private final UserStatusService userStatusService;
    private final UpdateService updateService;


    @SneakyThrows
    public void deleteMessagesFromChat(Long chatId, Long userId) {
        log.debug("----> вход в deleteMessagesFromChat()");
        DeleteMessage deleteMessage = new DeleteMessage();

        for (Integer messageId : userStatusService.getCurrentStatus(userId).getUserMessageIds()) {
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(chatId);
            remedyBot.execute(deleteMessage);
        }
        this.userStatusService.getCurrentStatus(userId).getUserMessageIds().clear();
        log.debug("<---- выход из deleteMessagesFromChat()");
    }

    @SneakyThrows
    public void deleteLastMessagesFromChat(Long chatId, Long userId, Integer qtyOfLastMessages) {
        log.debug("----> вход в deleteLastMessagesFromChat()");
        DeleteMessage deleteMessage = new DeleteMessage();

        Set<Integer> messagesSet = userStatusService.getCurrentStatus(userId).getUserMessageIds();
        Integer[] messagesArray = messagesSet.toArray(Integer[]::new);
        System.out.println(Arrays.toString(messagesArray));

        for (int x = 0; x < qtyOfLastMessages; x++) {
            deleteMessage.setMessageId(messagesArray[messagesArray.length - 1 - x]);
            System.out.println("Удаляем: " + messagesArray[messagesArray.length - 1 + x]);
            deleteMessage.setChatId(chatId);
            userStatusService.getCurrentStatus(userId).getUserMessageIds().remove(messagesArray[messagesArray.length - 1 + x]);
            remedyBot.execute(deleteMessage);
        }
        log.debug("<---- выход из deleteLastMessagesFromChat()");
    }

    public void addNewIdToMessageIds(Long userId, Integer messageId) {
        log.debug("----> вход в addNewIdToMessageIds(): {} - {}", userId, messageId);
        Status status = userStatusService.getCurrentStatus(userId);
        status.getUserMessageIds().add(messageId);
        userStatusService.changeCurrentStatus(userId, status);
        log.debug("<---- выход из addNewIdToMessageIds()");
    }

    public void clearMessageIds(Long userId) {
        log.debug("----> вход в clearMessageIds()");
        userStatusService.getCurrentStatus(userId).getUserMessageIds().clear();
        log.debug("<---- выход из clearMessageIds()");
    }

    public void showMedsNameList(Long chatId, Long userId, String medsListToChat) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(medsListToChat);
        sendMessage.setChatId(chatId);
        try {
            Message forDelete = remedyBot.execute(sendMessage);
            addNewIdToMessageIds(userId, forDelete.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
