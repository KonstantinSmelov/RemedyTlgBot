package com.smelov.service.impl;

import com.smelov.bot.CustomInlineKeyboardMarkup;
import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.Status;
import com.smelov.service.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotServiceImpl implements BotService {

    private final MedicineService medicineService;
    private final UserStatusService userStatusService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    //    private final CustomReplyKeyboardMarkup customReplyKeyboardMarkup;
    private final UpdateService updateService;
    private final TextMessageService textMessageService;

    @Override
    @SneakyThrows
    public BotApiMethod<?> onUpdateReceived(Update update) {
        log.info("=====> вход в onUpdateReceived() <=====");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Status status = userStatusService.getCurrentStatus(userId);
        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял в onUpdateReceived");

        //Обнуление статуса и выход в гл. меню из любого статуса
        if (update.hasMessage() && update.getMessage().getText().equals("/exit")) {
            log.info("Возвращаемся в гл. меню, обнуляем статус");
            userStatusService.resetStatus(userId);
            message.setReplyMarkup(new ReplyKeyboardRemove(true));
            message.setText("Вышли в главное меню");
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return message;
        }

        //Первичная отработка текстовых команд верхнего уровня (гл. меню) (Установка Status в ADD/EDIT/DEL)
        System.out.println(userStatusService.getStatusMap());
        if ((status != Status.NONE)) {
            message = currentStatusHandler(update, status);
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return message;
        } else if (update.hasCallbackQuery()) {
            message = callbackQueryHandler(update, userId);
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return message;
        } else {
            message = messageTextHandler(update, userId);
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return message;
        }
    }

    private SendMessage messageTextHandler(Update update, Long userId) {
        log.info("----> вход в messageTextHandler() <----");
        SendMessage message = new SendMessage();
        message.setText("Выберете пункт меню");

        message.setChatId(update.getMessage().getChatId());
        switch (update.getMessage().getText()) {
            case "/by_name":
                userStatusService.setCurrentStatus(userId, Status.NONE.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator((o1, o2) -> o1.getName().compareTo(o2.getName())).setMedicine(new Medicine()));
                message.setText(textMessageService.allInfoList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

            case "/by_exp_date":
                userStatusService.setCurrentStatus(userId, Status.NONE.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator((o1, o2) -> o1.getExpDate().compareTo(o2.getExpDate())).setMedicine(new Medicine()));
                message.setText(textMessageService.allInfoList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

            case "/start":
                message.setText("Добро пожаловать!\n\nЯ бот, который поможет в учёте лекарств в вашей домашней аптечке");
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
                userStatusService.setCurrentStatus(userId, Status.DEL.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "EDIT_BUTTON":
                log.info("EDIT_BUTTON");
                message.setText("Введите порядковый номер лекарства для редактирования:");
                userStatusService.setCurrentStatus(userId, Status.EDIT.setAddStatus(AddStatus.NONE).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
            case "ADD_BUTTON":
                log.info("ADD_BUTTON");
                message.setText("Введите название лекарства, которое вы хотите добавить:");
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.NAME).setEditStatus(EditStatus.NONE).setComparator(status.getComparator()).setMedicine(new Medicine()));
                break;
        }
        log.info("<---- выход из callbackQueryHandler() ---->");
        return message;
    }

    private SendMessage currentStatusHandler(Update update, Status currentStatus) {
        log.info("----> вход в currentStatusHandler() <----");
        Long userId = updateService.getUserId(update);

        switch (currentStatus) {
            case DEL:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.deleteMedByNumber(update, userStatusService.getCurrentStatus(userId).getComparator());
            case EDIT:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.editMedByNumber(update, userStatusService.getCurrentStatus(userId).getComparator());
            case ADD:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.addMedicine(update);
            default:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                SendMessage message = new SendMessage();
                message.setChatId(updateService.getChatId(update));
                message.setText("Простите, не понял в currentStatusHandler");
                return message;
        }
    }

}
