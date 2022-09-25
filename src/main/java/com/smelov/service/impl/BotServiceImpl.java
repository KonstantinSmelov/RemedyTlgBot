package com.smelov.service.impl;

import com.smelov.bot.CustomInlineKeyboardMarkup;
import com.smelov.bot.CustomReplyKeyboardMarkup;
import com.smelov.entity.Medicine;
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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotServiceImpl implements com.smelov.service.BotService {

    private final MedicineService medicineService;
    private final UserStatusService userStatusService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    private final CustomReplyKeyboardMarkup customReplyKeyboardMarkup;
    private final UpdateService updateService;
    private final TextMessageService textMessageService;

    @Override
    @SneakyThrows
    public SendMessage onUpdateReceived(Update update) {
        log.info("----> вход в onUpdateReceived() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Status status = userStatusService.getCurrentStatus(userId);

        if (status != Status.NONE) {
            message = currentStatusHandler(update, message, userId, status);
        }

//        switch (userStatusService.getCurrentStatus(userId)) {
//            case DEL:
//                log.debug("Получили статус {}", userStatusService.getCurrentStatus(userId));
//                log.info("<---- выход из onUpdateReceived() ---->");
//                return medicineService.deleteMedByNumber(update);
//            case EDIT:
//                log.debug("Получили статус {}", userStatusService.getCurrentStatus(userId));
//                log.info("<---- выход из onUpdateReceived() ---->");
//                return medicineService.editMedByNumber(update);
//            default:
//                break;
//        }

//        if (userStatusService.getCurrentStatus(userId) != Status.NONE) {
////            if(userStatusService.getCurrentStatus(userId).equals(Status.DEL)) {
////                log.debug("Получили статус {}", userStatusService.getCurrentStatus(userId));
////                log.info("<---- выход из onUpdateReceived() ---->");
////                return medicineService.deleteMedByNumber(update);
////            }
////            else
//            {
//                log.debug("Получили статус {}", userStatusService.getCurrentStatus(userId));
//                log.info("<---- выход из onUpdateReceived() ---->");
//                return medicineService.addMedicine(update);
//            }
//        }

        if (update.hasCallbackQuery()) {
            message = callbackQueryHandler(update, message, userId);
        }

        if (update.getMessage().hasText() || status == Status.NONE) {
            message = messageTextHandler(update, message, userId);
        }


        log.info("<---- выход из onUpdateReceived() ---->");
        return message;
    }

    private SendMessage messageTextHandler(Update update, SendMessage message, Long userId) {
        log.info("----> вход в messageTextHandler() <----");

        message.setChatId(update.getMessage().getChatId());
        switch (update.getMessage().getText()) {
            case "/detailed_list":
                message.setText(textMessageService.allMedInfoToText(medicineService.getAllMeds()));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

            case "/only_name_list":
                message.setText(textMessageService.medNameAndDosageToText(medicineService.getAllMeds()));
                break;

            case "/add":
                message.setText("Введите имя лекарства");
                userStatusService.setCurrentStatus(userId, Status.NAME.setMedicine(new Medicine()));
                log.info("Блок case '/add'. Добавили в Map: key:{}  value:{}", userId, userStatusService.getCurrentStatus(userId));
                log.debug("Блок case '/add'. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case "/del":
                message.setText("Введите порядковый номер лекарства для удаления");
                userStatusService.setCurrentStatus(userId, Status.DEL.setMedicine(new Medicine()));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                log.info("Блок case '/del'. Добавили в Map: key:{}  value:{}", userId, userStatusService.getCurrentStatus(userId));
                log.debug("Блок case '/add'. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case "/start":
                message.setText("Добро пожаловать!\n\nЯ бот, который поможет в учёте ваших лекарств");
                break;

            case "/test1":
                message.setText("Переключились на MainMenu");
                message.setReplyMarkup(customReplyKeyboardMarkup.replyKeyboardMarkupForDateMonth());
                break;

            case "/test2":
                message.setText("Переключились на Edit");
                message.setReplyMarkup(customReplyKeyboardMarkup.replyKeyboardMarkupForDateYears());
                break;

            case "/test3":
                message.setText("Удалили клаву");
                message.setReplyMarkup(new ReplyKeyboardRemove(true));
                break;

            default:
                message.setText("Простите, не понял");
                break;
        }
        log.info("<---- выход из messageTextHandler() ---->");
        return message;
    }

    private SendMessage callbackQueryHandler(Update update, SendMessage message, Long userId) {
        log.info("----> вход в callbackQueryHandler() <----");

        message.setChatId(updateService.getChatId(update));
        switch (update.getCallbackQuery().getData()) {
            case "DEL_BUTTON":
                log.info("DEL_BUTTON");
                message.setText("Введите порядковый номер лекарства для удаления");
                userStatusService.setCurrentStatus(userId, Status.DEL.setMedicine(new Medicine()));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                break;
            case "EDIT_BUTTON":
                message.setText("Введите порядковый номер лекарства для редактирования");
                userStatusService.setCurrentStatus(userId, Status.EDIT.setMedicine(new Medicine()));
                log.info("EDIT_BUTTON");
                break;
        }
        log.info("<---- выход из callbackQueryHandler() ---->");
        return message;
    }

    private SendMessage currentStatusHandler(Update update, SendMessage message, Long userId, Status currentStatus) {
        log.info("----> вход в currentStatusHandler() <----");

        switch (currentStatus) {
            case DEL:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.deleteMedByNumber(update);
            case EDIT:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.editMedByNumber(update);
            default:
                log.debug("Получили статус {}", currentStatus);
                log.info("<---- выход из currentStatusHandler() ---->");
                log.info("<---- выход из onUpdateReceived() ---->");
                return medicineService.addMedicine(update);
        }
    }
}
