package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagesService {

    private final RemedyBot remedyBot;
    private final UserStatusService userStatusService;


    @SneakyThrows
    public void deleteMessagesFromChat(Long chatId, Long userId) {
        log.debug("----> вход в deleteMessagesFromChat() <----");
        DeleteMessage deleteMessage = new DeleteMessage();

        for (Integer messageId : userStatusService.getCurrentStatus(userId).getUserMessageIds()) {
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(chatId);
//            System.out.print("Удаляем message/callback " + messageId + "... ");
            remedyBot.execute(deleteMessage);
//            System.out.println("ОК");
        }
        this.userStatusService.getCurrentStatus(userId).getUserMessageIds().clear();
        log.debug("<---- выход из deleteMessagesFromChat() ---->");

    }

    public void addNewIdToMessageIds(Integer messageId, Long userId) {
        log.debug("----> вход в addNewIdToMessageIds() <----");
        userStatusService.getCurrentStatus(userId).getUserMessageIds().add(messageId);
        log.debug("<---- выход из addNewIdToMessageIds() ---->");
    }

    public void clearMessageIds(Long userId) {
        log.debug("----> вход в clearMessageIds() <----");
        userStatusService.getCurrentStatus(userId).getUserMessageIds().clear();
        log.debug("<---- выход из clearMessageIds() ---->");
    }

//    public SendMessage appendMsgToMsg (SendMessage firstMsg, SendMessage secondMsg) {
//        SendMessage finalMsg = new SendMessage();
//        finalMsg.setChatId(firstMsg.getChatId());
//        finalMsg.setText(firstMsg.getText() + "\n\n" + secondMsg.getText());
//        finalMsg.setReplyMarkup(secondMsg.getReplyMarkup());
//        return finalMsg;
//    }
}
