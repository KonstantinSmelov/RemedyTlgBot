package com.smelov.handler;

import com.smelov.StaticClass;
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
import com.smelov.service.impl.PhotoService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateReceivedHandler {

    private final RemedyBot remedyBot;
    private final UpdateService updateService;
    private final UserStatusService userStatusService;
    private final TextMessageService textMessageService;
    private final MedicineService medicineService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    private final PhotoService photoService;

    @SneakyThrows
    public void onUpdateReceived(Update update) {
        log.info("=====> вход в onUpdateReceived() <=====");
        log.info("Map статусов {}", userStatusService.getStatusMap());

        Set<Integer> userMessageIds = StaticClass.userMessageIds;

        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Status status = userStatusService.getCurrentStatus(userId);
        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял в onUpdateReceived");


        if (update.hasMessage()) {
            StaticClass.userMessageIds.add(update.getMessage().getMessageId());
            System.out.println("Занесли user ID: " + update.getMessage().getMessageId());
            System.out.println("userMessageIds: " + StaticClass.userMessageIds);
        } else if (update.hasCallbackQuery()) {
            StaticClass.userMessageIds.add(update.getCallbackQuery().getMessage().getMessageId());
            System.out.println("Занесли user ID: " + update.getCallbackQuery().getMessage().getMessageId());
            System.out.println("userMessageIds: " + StaticClass.userMessageIds);
        }

        //Обнуление статуса и выход в гл. меню из любого статуса
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/exit")
                ||
                update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("CANCEL_BUTTON")) {
            log.info("Возвращаемся в гл. меню, обнуляем статус");
            userStatusService.resetStatus(userId);
            message.setReplyMarkup(new ReplyKeyboardRemove(true));
            message.setText(EmojiParser.parseToUnicode("Вышли в начальное состояние.\nНажмите кнопку меню\n" +
                    "   :arrow_down:"));
            log.info("<===== выход из onUpdateReceived() =====>\n");
            remedyBot.execute(message);
            StaticClass.userMessageIds = new HashSet<>();
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("Del")) {
            DeleteMessage deleteMessage = new DeleteMessage();

            for (Integer messageId : userMessageIds) {
                deleteMessage.setMessageId(messageId);
                deleteMessage.setChatId(updateService.getChatId(update));
                System.out.print("Удаляем message/callback " + messageId + "... ");
                remedyBot.execute(deleteMessage);
                System.out.println("ОК");
            }
            StaticClass.userMessageIds = new HashSet<>();
            System.out.println("После удаления: " + StaticClass.userMessageIds);
            return;
        }

        //Обработка текстовых команд верхнего уровня (гл. меню)
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().matches("^/.*")) {
            message = messageTextHandler(update);
            Message forDelete = remedyBot.execute(message);
            StaticClass.userMessageIds.add(forDelete.getMessageId());
            System.out.println("Занесли ID: " + forDelete.getMessageId());
            System.out.println("userMessageIds: " + StaticClass.userMessageIds);
            log.info("<===== выход из onUpdateReceived() =====>\n");

        } else if (status.getMainStatus() != MainStatus.NONE) {
            message = currentStatusHandler(update);
            Message forDelete = remedyBot.execute(message);
            StaticClass.userMessageIds.add(forDelete.getMessageId());
            System.out.println("Занесли ID: " + forDelete.getMessageId());
            System.out.println("userMessageIds: " + StaticClass.userMessageIds);
            log.info("<===== выход из onUpdateReceived() =====>\n");

        } else if (update.hasCallbackQuery()) {
            BotApiMethod<?> someBotApiMethod = callbackQueryHandler(update);

            System.out.println(someBotApiMethod.getClass().getName());

            if (someBotApiMethod.getClass().getName().equals("org.telegram.telegrambots.meta.api.methods.send.SendMessage")) {
                SendMessage sendMessage = (SendMessage) someBotApiMethod;
                Message forDelete = remedyBot.execute(sendMessage);
                StaticClass.userMessageIds.add(forDelete.getMessageId());
                System.out.println("Занесли ID: " + forDelete.getMessageId());
                System.out.println("userMessageIds: " + StaticClass.userMessageIds);
            }
            if (someBotApiMethod.getClass().getName().equals("org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText")) {
                EditMessageText editMessageText = (EditMessageText) someBotApiMethod;
                Message forDelete = (Message) remedyBot.execute(editMessageText);
                StaticClass.userMessageIds.add(forDelete.getMessageId());
                System.out.println("Занесли ID: " + forDelete.getMessageId());
                System.out.println("userMessageIds: " + StaticClass.userMessageIds);
            }
            log.info("<===== выход из onUpdateReceived() =====>\n");

        } else {
            message = messageTextHandler(update);
            Message forDelete = remedyBot.execute(message);
            StaticClass.userMessageIds.add(forDelete.getMessageId());
            log.info("<===== выход из onUpdateReceived() =====>\n");
            System.out.println("Занесли ID: " + forDelete.getMessageId());
            System.out.println("userMessageIds: " + StaticClass.userMessageIds);
        }
    }

    @SneakyThrows
    private SendMessage messageTextHandler(Update update) {
        log.info("----> вход в messageTextHandler() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);

        message.setText("Выберете пункт меню");

        message.setChatId(update.getMessage().getChatId());
        switch (update.getMessage().getText()) {
            case "/by_name":
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.NONE)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(Comparator.comparing(Medicine::getName))
                        .medicine(new Medicine())
                        .build());
                message.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

            case "/start":
                message.setText(EmojiParser.parseToUnicode("Добро пожаловать!\n\nЯ бот, который поможет в учёте лекарств в вашей домашней аптечке.\n\n" +
                        "Нажмите кнопку меню\n" +
                        "   :arrow_down:"));
                break;

            default:
                message.setText("Простите, не понял в messageTextHandler");
                break;
        }
        log.info("<---- выход из messageTextHandler() ---->");
        return message;
    }

    @SneakyThrows
    private BotApiMethod<?> callbackQueryHandler(Update update) {
        log.info("----> вход в callbackQueryHandler() <----");
        Status status = userStatusService.getCurrentStatus(updateService.getUserId(update));
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);

        message.setChatId(updateService.getChatId(update));

        switch (update.getCallbackQuery().getData()) {
            case "DEL_BUTTON":
                log.info("DEL_BUTTON");
                message.setText("Введите порядковый номер лекарства для удаления:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.DEL)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .build());
                break;

            case "EDIT_BUTTON":
                log.info("EDIT_BUTTON");
                message.setText("Введите порядковый номер лекарства для редактирования:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .build());
                break;

            case "ADD_BUTTON":
                log.info("ADD_BUTTON");
                message.setText("Введите название лекарства, которое вы хотите добавить:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.NAME)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .build());
                break;

            case "DETAIL_BUTTON":
                log.info("DETAIL_BUTTON");
                message.setText("Введите порядковый номер лекарства для показа деталей:");
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.DETAIL)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .build());
                break;

            case "DEL_FROM_DETAIL_BUTTON":
                log.info("DEL_FROM_DETAIL_BUTTON");
                message = medicineService.deleteMedByNumber(update);
                break;

            case "EDIT_FROM_DETAIL_BUTTON":
                log.info("EDIT_FROM_DETAIL_BUTTON");
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                Long chatId = updateService.getChatId(update);
                EditMessageText editedMessage = new EditMessageText();

                message = medicineService.getMedDetails(update);

                editedMessage.setChatId(chatId);
                editedMessage.setText(message.getText());
                editedMessage.setMessageId(messageId);
                editedMessage.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .medicine(status.getMedicine())
                        .comparator(status.getComparator())
                        .build());

                log.info("<---- выход из callbackQueryHandler() ---->");

                return editedMessage;
        }
        log.info("<---- выход из callbackQueryHandler() ---->");
        return message;
    }

    @SneakyThrows
    private SendMessage currentStatusHandler(Update update) {
        log.info("----> вход в currentStatusHandler() <----");
        Status status = userStatusService.getCurrentStatus(updateService.getUserId(update));
        SendMessage message = new SendMessage();

        switch (status.getMainStatus()) {
            case DEL:
                log.debug("Получили статус {}", status);
                message = medicineService.deleteMedByNumber(update);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case EDIT:
                log.debug("Получили статус {}", status);
                message = medicineService.editMedByNumber(update);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case ADD:
                log.debug("Получили статус {}", status);
                message = medicineService.addMedicine(update);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case DETAIL:
                log.debug("Получили статус {}", status);
                message = medicineService.getMedDetails(update);
                Medicine medicine = userStatusService.getCurrentStatus(updateService.getUserId(update)).getMedicine();
                photoService.showPhotoIfExist(update, medicine);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            default:
                log.debug("Получили статус {}", status);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                message.setChatId(updateService.getChatId(update));
                message.setText("Простите, не понял в currentStatusHandler");
                return message;
        }
    }
}
