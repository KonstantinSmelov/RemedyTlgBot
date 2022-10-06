package com.smelov.service.impl;

import com.smelov.config.BotConfig;
import com.smelov.keyboard.CustomInlineKeyboardMarkup;
import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.Status;
import com.smelov.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RemedyBot extends TelegramLongPollingBot {

    private final MedicineService medicineService;
    private final UserStatusService userStatusService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    private final UpdateService updateService;
    private final TextMessageService textMessageService;
    private final BotConfig botConfig;

    @SneakyThrows
    public RemedyBot(MedicineService medicineService, UserStatusService userStatusService, CustomInlineKeyboardMarkup customInlineKeyboardMarkup, UpdateService updateService, TextMessageService textMessageService, BotConfig botConfig) {
        this.medicineService = medicineService;
        this.userStatusService = userStatusService;
        this.customInlineKeyboardMarkup = customInlineKeyboardMarkup;
        this.updateService = updateService;
        this.textMessageService = textMessageService;
        this.botConfig = botConfig;

        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/by_name", "Список по названию"));
        commandList.add(new BotCommand("/by_exp_date", "Список по сроку годности"));
        commandList.add(new BotCommand("/exit", "Выход в главное меню"));
        execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        log.info("=====> вход в onUpdateReceived() <=====");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Status status = userStatusService.getCurrentStatus(userId);
        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял в onUpdateReceived");

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            log.info("----> вход в hasPhoto() <----");

            GetFile getFile = new GetFile();
            getFile.setFileId(update.getMessage().getPhoto().get(3).getFileId());
            File file = execute(getFile);
            downloadFile(file, new java.io.File("./src/main/resources/photo/" + String.valueOf(Math.random()).substring(3, 8) + ".jpg"));

            message.setText("Фото отправлено в БД");
            execute(message);
            log.info("<---- выход из hasPhoto() ---->");
            return;
        }

        //Обнуление статуса и выход в гл. меню из любого статуса
        if (update.hasMessage() && update.getMessage().getText().equals("/exit")) {
            log.info("Возвращаемся в гл. меню, обнуляем статус");
            userStatusService.resetStatus(userId);
            message.setReplyMarkup(new ReplyKeyboardRemove(true));
            message.setText("Вышли в главное меню");
            execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return;
        }


        //Обработка текстовых команд верхнего уровня (гл. меню) (Установка Status в ADD/EDIT/DEL)
        if ((status != Status.NONE)) {
            message = currentStatusHandler(update, status);
            execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else if (update.hasCallbackQuery()) {
            message = callbackQueryHandler(update, userId);
            execute(message);
            log.info("<===== выход из onUpdateReceived() =====>\n");
        } else {
            message = messageTextHandler(update, userId);
            execute(message);
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

            case "/photo":
                SendPhoto photo = new SendPhoto();
                photo.setChatId(userId);

                InputFile inputFile = new InputFile();
                inputFile.setMedia(new java.io.File("src/main/resources/photo", "21144.jpg"));

                photo.setPhoto(inputFile);
                try {
                    execute(photo);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                message.setText("Выслано фото");
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
