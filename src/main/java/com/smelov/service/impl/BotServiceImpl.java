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
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);

        if (userStatusService.getCurrentStatus(userId) != null) {
            if(userStatusService.getCurrentStatus(userId).equals(Status.DEL)) {
                log.info("Отрабатываем статус DEL");
                return medicineService.deleteMedByNumber(update);
            } else
            {
                return medicineService.addMedicine(update);
            }
        }

        if (update.getMessage().hasText()) {
            message.setChatId(update.getMessage().getChatId());

            switch (update.getMessage().getText()) {
                case "/detailed_list":
                    message.setText(textMessageService.allMedInfoToText(medicineService.getAllMeds()));
                    break;

                case "/only_name_list":
                    message.setText(textMessageService.medNameAndDosageToText(medicineService.getAllMeds()));
                    break;

                case "/add":
                    message.setText("Введите имя лекарства");
                    log.info("Введите имя лекарства");
                    userStatusService.setCurrentStatus(userId, Status.NAME.setMedicine(new Medicine()));
                    log.info("Блок case '/add'.  Добавили в Map {}", userStatusService.getCurrentStatus(userId));
                    break;

                case "/del":
                    message.setText("Введите порядковый номер лекарства для удаления");
                    userStatusService.setCurrentStatus(userId, Status.DEL.setMedicine(new Medicine()));
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                    log.info("Блок case '/del'.  Добавили в Map {} - {}", userId, Status.DEL);
                    log.info("{}", userStatusService.getStatusMap());
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
        }

        log.info("перед return message");
        return message;
    }


}
