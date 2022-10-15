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
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    @SneakyThrows
    public void onUpdateReceived(Update update) {
        log.info("=====> вход в onUpdateReceived() <=====");

        System.out.println(userStatusService.getStatusMap());

        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Status status = userStatusService.getCurrentStatus(userId);
        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял в onUpdateReceived");

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
            return;
        }

        //Обработка текстовых команд верхнего уровня (гл. меню)
        if (status.getMainStatus() != MainStatus.NONE) {
            message = currentStatusHandler(update, status);
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else if (update.hasCallbackQuery()) {
            BotApiMethod<?> message2 = callbackQueryHandler(update, status);
            remedyBot.execute(message2);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else {
            message = messageTextHandler(update, status);
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        }
    }

    @SneakyThrows
    private SendMessage messageTextHandler(Update update, Status status) {
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
                        .comparator((o1, o2) -> o1.getName().compareTo(o2.getName()))
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

    private BotApiMethod<?> callbackQueryHandler(Update update, Status status) {
        log.info("----> вход в callbackQueryHandler() <----");
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
                message = medicineService.deleteMedByNumber(update, status);
                break;
            case "EDIT_FROM_DETAIL_BUTTON":
                log.info("EDIT_FROM_DETAIL_BUTTON");
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                Long chatId = updateService.getChatId(update);
                EditMessageText editedMessage = new EditMessageText();

                message = medicineService.getDetailsByMedicine(update, status.getMedicine());

                editedMessage.setChatId(chatId);
                editedMessage.setText(message.getText());
                editedMessage.setMessageId(messageId);
                editedMessage.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                userStatusService.setCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .medicine(status.getMedicine())
                        .build());
                return editedMessage;

        }
        log.info("<---- выход из callbackQueryHandler() ---->");
        return message;
    }

    @SneakyThrows
    private SendMessage currentStatusHandler(Update update, Status status) {
        log.info("----> вход в currentStatusHandler() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);

        switch (status.getMainStatus()) {
            case DEL:
                log.debug("Получили статус {}", status);
                message = medicineService.deleteMedByNumber(update, userStatusService.getCurrentStatus(userId));
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case EDIT:
                log.debug("Получили статус {}", status);
                message = medicineService.editMedByNumber(update, userStatusService.getCurrentStatus(userId));
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
                message = medicineService.getDetailsByNumber(update, userStatusService.getCurrentStatus(userId));
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
