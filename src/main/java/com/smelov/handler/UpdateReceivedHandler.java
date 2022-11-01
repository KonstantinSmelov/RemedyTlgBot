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
import com.smelov.service.impl.ChatMessagesService;
import com.smelov.service.impl.PhotoService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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
    private final ChatMessagesService chatMessagesService;

    @SneakyThrows
    public void onUpdateReceived(Update update) {
        log.info("=====> вход в onUpdateReceived()");
        log.debug("Map статусов {}", userStatusService.getStatusMap());

        StaticClass.proceed = true;

        SendMessage sendMessage = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Простите, не понял в onUpdateReceived");


        if (update.hasMessage()) {
            chatMessagesService.addNewIdToMessageIds(userId, update.getMessage().getMessageId());
        } else if (update.hasCallbackQuery()) {
            chatMessagesService.addNewIdToMessageIds(userId, update.getCallbackQuery().getMessage().getMessageId());
        }

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("Del")) {
            chatMessagesService.deleteMessagesFromChat(chatId, userId);
            return;
        }

        //Обработка текстовых команд верхнего уровня (гл. меню)
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().matches("^/.*")) {
            sendMessage = messageTextHandler(update);
            Message forDelete = remedyBot.execute(sendMessage);
            chatMessagesService.addNewIdToMessageIds(userId, forDelete.getMessageId());
            log.info("<===== выход из onUpdateReceived() =====>\n");
            return;

        } else if (userStatusService.getCurrentStatus(userId).getMainStatus().equals(MainStatus.MAIN_MENU)
                &&
                !update.hasCallbackQuery()
                &&
                !update.hasMessage()) {
            chatMessagesService.deleteMessagesFromChat(chatId, userId);
            userStatusService.changeCurrentStatus(userId, Status.builder()
                    .mainStatus(MainStatus.NONE)
                    .addStatus(AddStatus.NONE)
                    .editStatus(EditStatus.NONE)
                    .comparator(Comparator.comparing(Medicine::getName))
                    .medicine(new Medicine())
                    .userMessageIds(new LinkedHashSet<>())
                    .build());
            sendMessage.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
            sendMessage.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
            remedyBot.execute(sendMessage);
            chatMessagesService.clearMessageIds(userId);
            log.info("<===== выход из onUpdateReceived()\n");
            return;

        } else if (userStatusService.getCurrentStatus(userId).getMainStatus() != MainStatus.MAIN_MENU) {

            while (StaticClass.proceed) {
                StaticClass.proceed = false;
                BotApiMethod<?> sendMessage1 = currentStatusHandler(update);
                Message forDelete = (Message) remedyBot.execute(sendMessage1);
                chatMessagesService.addNewIdToMessageIds(userId, forDelete.getMessageId());
                log.info("<===== выход из onUpdateReceived()\n");
            }
            return;
        } else if (update.hasCallbackQuery()) {
            BotApiMethod<?> someBotApiMethod = callbackQueryHandler(update);
            Message forDelete = (Message) remedyBot.execute(someBotApiMethod);
            chatMessagesService.addNewIdToMessageIds(userId, forDelete.getMessageId());
            log.info("<===== выход из onUpdateReceived()\n");
            return;

        } else {
            sendMessage = messageTextHandler(update);
            Message forDelete = remedyBot.execute(sendMessage);
            chatMessagesService.addNewIdToMessageIds(userId, forDelete.getMessageId());
            log.info("<===== выход из onUpdateReceived()\n");
        }
    }

    @SneakyThrows
    private SendMessage messageTextHandler(Update update) {
        log.info("----> вход в messageTextHandler()");
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        SendMessage message = new SendMessage();

        message.setChatId(update.getMessage().getChatId());
        switch (update.getMessage().getText()) {
            case "/by_name":
                log.info("case /by_name");
//                chatMessagesService.deleteMessagesFromChat(chatId, userId);
//                userStatusService.changeCurrentStatus(userId, Status.builder()
//                        .mainStatus(MainStatus.MAIN_MENU)
//                        .addStatus(AddStatus.NONE)
//                        .editStatus(EditStatus.NONE)
//                        .comparator(Comparator.comparing(Medicine::getName))
//                        .medicine(new Medicine())
//                        .userMessageIds(new HashSet<>())
//                        .build());
//                message.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
//                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
//                break;

            case "/start":
                log.info("case /start");
//                message.setText(EmojiParser.parseToUnicode("Добро пожаловать!\n\nЯ бот, который поможет в учёте лекарств в вашей домашней аптечке.\n\n" +
//                        "Нажмите кнопку меню\n" +
//                        "   :arrow_down:"));
                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.MAIN_MENU)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(Comparator.comparing(Medicine::getName))
                        .medicine(new Medicine())
                        .build());
                message.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                break;

            default:
//                message.setText("Простите, не понял в messageTextHandler");
                message.setText(EmojiParser.parseToUnicode("Простите, не понял.\nНажмите Меню -> Список лекарств\n   :arrow_down:"));
                break;
        }
        log.info("<---- выход из messageTextHandler()");
        return message;
    }

    @SneakyThrows
    private BotApiMethod<?> callbackQueryHandler(Update update) {
        log.info("----> вход в callbackQueryHandler()");
        Status status = userStatusService.getCurrentStatus(updateService.getUserId(update));
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);

        message.setChatId(updateService.getChatId(update));

        switch (update.getCallbackQuery().getData()) {
            case "DEL_BUTTON":
                log.info("DEL_BUTTON");
                message.setText("Введите порядковый номер лекарства для удаления:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                userStatusService.changeCurrentStatus(userId, Status.builder()
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
                userStatusService.changeCurrentStatus(userId, Status.builder()
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
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.NAME)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .build());
                chatMessagesService.deleteMessagesFromChat(chatId, userId);

                break;

            case "DETAIL_BUTTON":
                log.info("DETAIL_BUTTON");
                message.setText("Введите порядковый номер лекарства для показа деталей:");
                System.out.println("в case \"DETAIL_BUTTON\"" + userStatusService.getCurrentStatus(userId).getUserMessageIds());
                userStatusService.changeCurrentStatus(userId,
                        Status.builder()
                                .mainStatus(MainStatus.DETAIL)
                                .addStatus(AddStatus.NONE)
                                .editStatus(EditStatus.NONE)
                                .comparator(status.getComparator())
                                .build());
                System.out.println("в case \"DETAIL_BUTTON\"" + userStatusService.getCurrentStatus(userId).getUserMessageIds());
                break;

            default:
                message.setText("Не знаю статуса в callbackQueryHandler");
                break;
        }
        log.info("<---- выход из callbackQueryHandler()");
        return message;
    }

    @SneakyThrows
    private BotApiMethod<?> currentStatusHandler(Update update) {
        log.info("----> вход в currentStatusHandler()");
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        Status status = userStatusService.getCurrentStatus(updateService.getUserId(update));
        SendMessage sendMessage = new SendMessage();

        switch (status.getMainStatus()) {

            case MAIN_MENU:
                log.debug("case MAIN_MENU");
                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                StaticClass.proceed = false;
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.MAIN_MENU)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(Comparator.comparing(Medicine::getName))
                        .medicine(new Medicine())
                        .userMessageIds(new LinkedHashSet<>())
                        .build());
                sendMessage.setChatId(updateService.getChatId(update));
                sendMessage.setText(textMessageService.nameList(medicineService.getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                sendMessage.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForAllMedsList());
                return sendMessage;

            case DEL:
                log.info("case DEL");
                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                sendMessage = medicineService.deleteMedByNumber(update);
                log.info("<---- выход из currentStatusHandler()");
                log.info("<---- выход из onUpdateReceived()");
                return sendMessage;

            case EDIT:
                log.info("case EDIT");
                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                sendMessage = medicineService.editMedByNumber(update);
                log.info("<---- выход из currentStatusHandler()");
                log.info("<---- выход из onUpdateReceived()");
                return sendMessage;

            case ADD:
                log.info("case ADD");
                sendMessage = medicineService.addMedicine(update);
                log.info("<---- выход из currentStatusHandler()");
                log.info("<---- выход из onUpdateReceived()");
                return sendMessage;

            case DETAIL:
                log.info("case DETAIL");
                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                BotApiMethod<?> sendMessage1 = medicineService.getMedDetails(update);
                Medicine medicine = userStatusService.getCurrentStatus(updateService.getUserId(update)).getMedicine();
                photoService.showPhotoIfExist(update, medicine);
                log.info("<---- выход из currentStatusHandler()");
                log.info("<---- выход из onUpdateReceived(");
                return sendMessage1;

            default:
                log.info("case default");
                sendMessage.setChatId(chatId);
                sendMessage.setText("Простите, не понял в currentStatusHandler");
                log.info("<---- выход из currentStatusHandler()");
                log.info("<---- выход из onUpdateReceived()");
                return sendMessage;
        }
    }
}
