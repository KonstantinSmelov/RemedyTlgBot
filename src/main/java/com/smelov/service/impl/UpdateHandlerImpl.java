package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.entity.Medicine;
import com.smelov.keyboard.CustomInlineKeyboardMarkup;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlerImpl {

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
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return;
        }

        //Обработка текстовых команд верхнего уровня (гл. меню)
        if (status != Status.NONE) {
            message = currentStatusHandler(update, status);
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else if (update.hasCallbackQuery()) {
            message = callbackQueryHandler(update, userId);
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else {
            message = messageTextHandler(update, userId);
            remedyBot.execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        }
    }

    @SneakyThrows
    private SendMessage messageTextHandler(Update update, Long userId) {
        log.info("----> вход в messageTextHandler() <----");
        SendMessage message = new SendMessage();
        message.setText("Выберете пункт меню");

        message.setChatId(update.getMessage().getChatId());
        switch (update.getMessage().getText()) {
            case "/by_name":
                userStatusService.setCurrentStatus(userId, Status.NONE.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator((o1, o2) -> o1.getName().compareTo(o2.getName())).setMedicine(new Medicine()));
                message.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

//            case "/by_exp_date":
//                userStatusService.setCurrentStatus(userId, Status.NONE.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator((o1, o2) -> o1.getExpDate().compareTo(o2.getExpDate())).setMedicine(new Medicine()));
//                message.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
//                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
//                break;

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

    private SendMessage callbackQueryHandler(Update update, Long userId) {
        log.info("----> вход в callbackQueryHandler() <----");
        SendMessage message = new SendMessage();
        Status status = userStatusService.getCurrentStatus(userId);

        message.setChatId(updateService.getChatId(update));
        switch (update.getCallbackQuery().getData()) {
            case "DEL_BUTTON":
                log.info("DEL_BUTTON");
                message.setText("Введите порядковый номер лекарства для удаления:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.DEL.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "EDIT_BUTTON":
                log.info("EDIT_BUTTON");
                message.setText("Введите порядковый номер лекарства для редактирования:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.EDIT.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "ADD_BUTTON":
                log.info("ADD_BUTTON");
                message.setText("Введите название лекарства, которое вы хотите добавить:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.NAME).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "DETAIL_BUTTON":
                log.info("DETAIL_BUTTON");
                message.setText("Введите порядковый номер лекарства для показа деталей:");
                userStatusService.setCurrentStatus(userId, Status.DETAIL.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "DEL_FROM_DETAIL_BUTTON":
                log.info("DEL_FROM_DETAIL_BUTTON");
                message.setText("Пробуем удалить");
                System.out.println(userStatusService.getStatusMap());
                medicineService.deleteMedByNumber(userStatusService.getCurrentStatus(userId).getMedicine());
                userStatusService.resetStatus(userId);
                break;
        }
        log.info("<---- выход из callbackQueryHandler() ---->");
        return message;
    }

    @SneakyThrows
    private SendMessage currentStatusHandler(Update update, Status currentStatus) {
        log.info("----> вход в currentStatusHandler() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);

        switch (currentStatus) {
            case DEL:
                log.debug("Получили статус {}", currentStatus);
                message = medicineService.deleteMedByNumber(update, userStatusService.getCurrentStatus(userId));
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case EDIT:
                log.debug("Получили статус {}", currentStatus);
                message = medicineService.editMedByNumber(update, userStatusService.getCurrentStatus(userId));
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case ADD:
                log.debug("Получили статус {}", currentStatus);
                message = medicineService.addMedicine(update);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            case DETAIL:
                log.debug("Получили статус {}", currentStatus);
                message = medicineService.getDetailsByNumber(update, userStatusService.getCurrentStatus(userId));
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return message;

            default:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                message.setChatId(updateService.getChatId(update));
                message.setText("Простите, не понял в currentStatusHandler");
                return message;
        }
    }
}
