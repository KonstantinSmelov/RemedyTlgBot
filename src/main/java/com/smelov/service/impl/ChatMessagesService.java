package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.model.Status;
import com.smelov.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagesService {

    private final RemedyBot remedyBot;
    private final UserStatusService userStatusService;


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
            deleteMessage.setMessageId(messagesArray[messagesArray.length - qtyOfLastMessages + x]);
            System.out.println("Удаляем: " + messagesArray[messagesArray.length - qtyOfLastMessages + x]);
            deleteMessage.setChatId(chatId);
            userStatusService.getCurrentStatus(userId).getUserMessageIds().remove(messagesArray[messagesArray.length - qtyOfLastMessages + x]);
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
}
