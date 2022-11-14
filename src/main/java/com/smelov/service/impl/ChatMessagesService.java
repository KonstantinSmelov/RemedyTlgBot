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

    public void deleteMessagesFromChat(Long chatId, Long userId) {
        log.debug("----> вход в deleteMessagesFromChat()");
        DeleteMessage deleteMessage = new DeleteMessage();

        for (Integer messageId : userStatusService.getCurrentStatus(userId).getUserMessageIds()) {
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(chatId);
            executeOrRemove(deleteMessage, userId, messageId);
        }
        this.userStatusService.getCurrentStatus(userId).getUserMessageIds().clear();
        log.debug("<---- выход из deleteMessagesFromChat()");
    }

    //    @SneakyThrows
    public void deleteLastMessagesFromChat(Long chatId, Long userId, Integer qtyOfLastMessages) {
        log.debug("----> вход в deleteLastMessagesFromChat()");
        DeleteMessage deleteMessage = new DeleteMessage();
        Integer messageId;

        Set<Integer> messageIdSet = userStatusService.getCurrentStatus(userId).getUserMessageIds();
        Integer[] messageIdArray = messageIdSet.toArray(Integer[]::new);
        System.out.println(Arrays.toString(messageIdArray));

        for (int x = 0; x < qtyOfLastMessages; x++) {
            messageId = messageIdArray[messageIdArray.length - 1 + x];
            deleteMessage.setMessageId(messageIdArray[messageIdArray.length - 1 - x]);
            System.out.println("Удаляем: " + messageIdArray[messageIdArray.length - 1 + x]);
            deleteMessage.setChatId(chatId);
            executeOrRemove(deleteMessage, userId, messageId);
            userStatusService.getCurrentStatus(userId).getUserMessageIds().remove(messageId);
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
        sendMessage.enableHtml(true);
        sendMessage.setText(medsListToChat);
        sendMessage.setChatId(chatId);
        try {
            Message forDelete = remedyBot.execute(sendMessage);
            addNewIdToMessageIds(userId, forDelete.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeOrRemove(DeleteMessage deleteMessage, Long userId, Integer messageId) {
        try {
            remedyBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println("кэч сработал");
            userStatusService.getCurrentStatus(userId).getUserMessageIds().remove(messageId);
        }
    }
}
