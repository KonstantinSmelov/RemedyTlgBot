package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.entity.Medicine;
import com.smelov.keyboard.CustomInlineKeyboardMarkup;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.MainStatus;
import com.smelov.model.Status;
import com.smelov.service.MedicineService;
import com.smelov.service.TextMessageService;
import com.smelov.service.UpdateService;
import com.smelov.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagesService {

    private final UpdateService updateService;
    private final RemedyBot remedyBot;
    private final Set<Integer> messageIds = new HashSet<>();
    private final UserStatusService userStatusService;

    @SneakyThrows
    public void deleteMessagesFromChat(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        Long chatId = updateService.getChatId(update);

        for (Integer messageId : messageIds) {
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(chatId);
            System.out.print("Удаляем message/callback " + messageId + "... ");
            remedyBot.execute(deleteMessage);
            System.out.println("ОК");
        }
        this.messageIds.clear();
    }

    public void addNewIdToMessageIds(Integer messageId) {
        this.messageIds.add(messageId);
    }

    public void clearMessageIds() {
        this.messageIds.clear();
    }

    public SendMessage appendMsgToMsg (SendMessage firstMsg, SendMessage secondMsg) {
        SendMessage finalMsg = new SendMessage();
        finalMsg.setChatId(firstMsg.getChatId());
        finalMsg.setText(firstMsg.getText() + "\n\n" + secondMsg.getText());
        finalMsg.setReplyMarkup(secondMsg.getReplyMarkup());
        return finalMsg;
    }
}
