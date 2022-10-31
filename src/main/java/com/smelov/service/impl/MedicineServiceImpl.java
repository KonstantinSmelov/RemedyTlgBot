package com.smelov.service.impl;

import com.smelov.StaticClass;
import com.smelov.bot.RemedyBot;
import com.smelov.keyboard.CustomInlineKeyboardMarkup;
import com.smelov.dao.MedicineRepository;
import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.MainStatus;
import com.smelov.model.Status;
import com.smelov.service.DateService;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.sql.Date;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserStatusServiceImpl userStatusService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    private final UpdateService updateService;
    private final DateService dateService;
    private final RemedyBot remedyBot;
    private final ChatMessagesService chatMessagesService;

    @Override
    public List<Medicine> getAllMeds(Comparator<Medicine> comparator) {
        log.debug("----> вход в getAllMeds()");
        List<Medicine> meds = medicineRepository.findAll();
        log.trace("getAllMeds(): получили список из БД: {}", meds.stream().map(Medicine::getName).collect(Collectors.toList()));
        meds.sort(comparator);
        log.trace("getAllMeds(): отсортировали список: {}", meds.stream().map(Medicine::getName).collect(Collectors.toList()));
        log.debug("<---- выход из getAllMeds(): {}", meds);
        return meds;
    }

    @Override
    public SendMessage editMedByNumber(Update update) {
        log.info("----> вход в editMedByNumber()");

        StaticClass.proceed = false;
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        message.setChatId(updateService.getChatId(update));
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine medicine = status.getMedicine();
        Medicine medToEdit;

        if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case "EDIT_NAME_BUTTON":
                    log.info("case EDIT_NAME_BUTTON");
                    message.setText(String.format("Заменить [%s] на:", medicine.getName()));
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.EDIT_NAME)
                            .medicine(medicine)
                            .build());
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForReturn());
                    log.info("<---- выход из editMedByNumber() ---->");
                    return message;

                case "EDIT_DOSAGE_BUTTON":
                    log.info("case EDIT_DOSAGE_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    if (extractDataWithoutUnits(medicine.getDosage()).equals("---")) {
                        message.setText("Для данного препарата дозировка не применяется");
                        userStatusService.changeCurrentStatus(userId, Status.builder()
                                .mainStatus(MainStatus.EDIT)
                                .addStatus(AddStatus.NONE)
                                .editStatus(EditStatus.NONE)
                                .comparator(status.getComparator())
                                .medicine(medicine)
                                .build());
                    } else {
                        message.setText(String.format("Заменить [%s] на:", extractDataWithoutUnits(medicine.getDosage())));
                        userStatusService.changeCurrentStatus(userId, Status.builder()
                                .mainStatus(MainStatus.EDIT)
                                .addStatus(AddStatus.NONE)
                                .editStatus(EditStatus.EDIT_DOSAGE)
                                .medicine(medicine)
                                .build());
                    }
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForReturn());

                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "EDIT_QUANTITY_BUTTON":
                    log.info("case EDIT_QUANTITY_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на:", extractDataWithoutUnits(medicine.getQuantity())));
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.EDIT_QTY)
                            .medicine(medicine)
                            .build());
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForReturn());
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "EDIT_EXP_DATE_BUTTON":
                    log.info("case EDIT_EXP_DATE_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на (введите год и месяц через пробел):", medicine.getTextExpDate()));
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.EDIT_EXP)
                            .medicine(medicine)
                            .build());
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForReturn());
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "EDIT_PHOTO_BUTTON":
                    log.info("case EDIT_PHOTO_BUTTON");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                    SendPhoto photo = getMedicinePhoto(medicine);
                    if ((photo == null)) {
                        message.setText(String.format("Фото для [%s] ранее не было назначено!\nСделайте фото:", medicine.getName() +
                                ((medicine.getDosage().equals("---"))
                                        ? ""
                                        : (" - " + medicine.getDosage()))));
                    } else {
                        message.setText(String.format("Сделайте новое фото для [%s]:", medicine.getName() +
                                ((medicine.getDosage().equals("---"))
                                        ? ""
                                        : (" - " + medicine.getDosage()))));
                    }
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.EDIT_PHOTO)
                            .medicine(medicine)
                            .build());
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForReturn());
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "EDIT_BUTTON":
                    log.info("case EDIT_BUTTON");
                    message.setText("Введите порядковый номер лекарства для редактирования:");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(status.getComparator())
                            .build());
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "MAIN_MENU_BUTTON":
                    log.info("case MAIN_MENU_BUTTON");
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.MAIN_MENU)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
//                            .medicine(new Medicine())
                            .build());
                    message.setText("Выходим в главное меню...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "RETURN_BUTTON":
                    log.info("case RETURN_BUTTON");
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
                            .medicine(userStatusService.getCurrentStatus(userId).getMedicine())
                            .build());
                    message.setText("Возврат...");
                    StaticClass.proceed = true;
                    update.getCallbackQuery().setData("");
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "case CANCEL_BUTTON":
                    log.info("CANCEL_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText("Отмена...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из editMedByNumber()");
                    return message;
            }
        }

        switch (status.getEditStatus()) {
            case NONE:
                log.info("case NONE");

                if (medicine.getName() == null) {
                    medicine = getMedByNumber(textFromChat, status.getComparator());
                }

                if (medicine != null) {
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    System.out.println(medicine.getName() + " / " + medicine.getExpDate());
                    status.setMedicine(medicine);
                    message.setText("Препарат..... " + medicine.getName()
                            + "\nДозировка.. " + medicine.getDosage()
                            + "\nКол-во.......... " + medicine.getQuantity()
                            + "\nГоден до....... " + medicine.getTextExpDate()
                            + "\n\nЧто меняем?");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                } else {
                    message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
                }
                break;

            case EDIT_NAME:
                log.info("case EDIT_NAME");
                medToEdit = new Medicine(medicine);
                medToEdit.setName(textFromChat);
                if (edit(medToEdit, medicine)) {
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    renamePhotoFile(medicine, medToEdit);
                    message.setText(String.format("%s изменено на %s", medicine.getName(), medToEdit.getName()));
                } else {
                    message.setText("Название НЕ изменено");
                }
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .medicine(medToEdit)
                        .build());
                StaticClass.proceed = true;
                break;

            case EDIT_DOSAGE:
                log.info("case EDIT_DOSAGE");
                medToEdit = new Medicine(medicine);
                medToEdit.setDosage(textFromChat + medicine.getDosage().substring(medicine.getDosage().length() - 4));
                if (edit(medToEdit, medicine)) {
                    renamePhotoFile(medicine, medToEdit);
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText("Дозировка изменена");
                } else {
                    message.setText("Дозировка НЕ изменена");
                }
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .medicine(medToEdit)
                        .build());
                StaticClass.proceed = true;
                break;

            case EDIT_QTY:
                log.info("case EDIT_QTY");
                medToEdit = new Medicine(medicine);
                medToEdit.setQuantity(textFromChat + " " + extractUnitsWithoutData(medicine.getQuantity()));
                if (edit(medToEdit, medicine)) {
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText("Количество изменено");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
                } else {
                    message.setText("Количество НЕ изменено");
                }
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .medicine(medToEdit)
                        .build());
                StaticClass.proceed = true;
                break;

            case EDIT_EXP:
                log.info("case EDIT_QTY");
                medToEdit = new Medicine(medicine);
                Optional<Date> optionalDate = dateService.StrToDate(textFromChat);
                if (optionalDate.isPresent()) {
                    medToEdit.setExpDate(optionalDate.get());
                    if (edit(medToEdit, medicine)) {
                        chatMessagesService.deleteMessagesFromChat(chatId, userId);
                        userStatusService.resetStatus(chatId, userId);
                        message.setText("Срок годности изменён");
                    } else {
                        message.setText("Срок годности НЕ изменён");
                    }
                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                }
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .medicine(medToEdit)
                        .build());
                StaticClass.proceed = true;
                break;

            case EDIT_PHOTO:
                log.info("case EDIT_PHOTO");

                deleteMedicinePhoto(medicine);
                setMedicinePhoto(update, medicine);

                chatMessagesService.deleteMessagesFromChat(chatId, userId);
                userStatusService.resetStatus(chatId, userId);
                message.setText("Фото изменено");
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.EDIT)
                        .addStatus(AddStatus.NONE)
                        .editStatus(EditStatus.NONE)
                        .comparator(status.getComparator())
                        .medicine(medicine)
                        .build());
                StaticClass.proceed = true;
                break;
        }
        log.info("<---- выход из editMedByNumber()");
        return message;
    }

    @Override
    public BotApiMethod<?> getMedDetails(Update update) {
        log.info("----> вход в getMedDetails()");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        message.setChatId(chatId);
        Status status = userStatusService.getCurrentStatus(userId);

        if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {

                case "DEL_FROM_DETAIL_BUTTON":
                    log.info("case DEL_FROM_DETAIL_BUTTON");
                    return deleteMedByNumber(update);

                case "EDIT_FROM_DETAIL_BUTTON":
                    log.info("case EDIT_FROM_DETAIL_BUTTON");
//                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//                    Long chatId1 = updateService.getChatId(update);

                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    message = getDetailsByNumber(update);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());


//                    EditMessageText editedMessage = new EditMessageText();
//                    editedMessage.setChatId(chatId1);
//                    editedMessage.setText(message.getText());
//                    editedMessage.setMessageId(messageId);
//                    editedMessage.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());

                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .medicine(status.getMedicine())
                            .comparator(status.getComparator())
                            .build());
//                    return editedMessage;
                    return message;

                case "MAIN_MENU_BUTTON":
                    log.info("MAIN_MENU_BUTTON");
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.MAIN_MENU)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
//                            .medicine(new Medicine())
                            .build());
                    message.setText("Выходим в главное меню...");
                    StaticClass.proceed = true;
                    return message;
            }
        }

        return getDetailsByNumber(update);
    }

    @SneakyThrows
    @Override
    public SendPhoto getMedicinePhoto(Medicine medicine) {
        log.debug("----> вход в getMedicinePhoto()");
        SendPhoto photo = null;
        java.io.File file = new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg");
        if (file.exists()) {
            photo = new SendPhoto();
            InputFile inputFile = new InputFile();
            inputFile.setMedia(file);
            photo.setPhoto(inputFile);
        }
        log.debug("<---- выход из getMedicinePhoto(): {}", file.getAbsolutePath());
        return photo;
    }

    @Override
    public SendMessage deleteMedByNumber(Update update) {
        log.info("----> вход в deleteMedByNumber()");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        message.setChatId(chatId);
        String textFromChat = updateService.getTextFromMessage(update);
        Status currentStatus = userStatusService.getCurrentStatus(userId);
        Medicine medicine = currentStatus.getMedicine();

        if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case "MAIN_MENU_BUTTON":
                    log.info("case MAIN_MENU_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.MAIN_MENU)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
                            .userMessageIds(new HashSet<>())
                            .build());
                    message.setText("Выходим в главное меню...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из deleteMedByNumber()");
                    return message;

                case "CANCEL_BUTTON":
                    log.info("case CANCEL_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText("Отмена...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из deleteMedByNumber()");
                    return message;
            }
        }

        if (medicine.getName() == null) {
            medicine = getMedByNumber(textFromChat, currentStatus.getComparator());
        }

        if (medicine != null) {
            log.debug("Нашли лекарство {}", medicine);
            medicineRepository.deleteByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
            deleteMedicinePhoto(medicine);
            message.setText("Вы удалили:\n\n" +
                    "Препарат..... " + medicine.getName()
                    + "\nДозировка.. " + medicine.getDosage()
                    + "\nКол-во.......... " + medicine.getQuantity()
                    + "\nГоден до....... " + medicine.getTextExpDate());
//            userStatusService.resetStatus(userId);
            message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
        } else {
            message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
//            message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
        }

        log.info("<---- выход из deleteMedByNumber()");
        return message;
    }

    @Override
    public SendMessage addMedicine(Update update) {
        log.info("----> вход в addMedicine()");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);

        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял, начните с начала (в addMedicine)");

        if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case "MAIN_MENU_BUTTON":
                    log.info("case MAIN_MENU_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.MAIN_MENU)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
                            .userMessageIds(new HashSet<>())
                            .build());
                    message.setText("Выходим в главное меню...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из addMedicine()");
                    return message;

                case "CANCEL_BUTTON":
                    log.info("case CANCEL_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText("Отмена...");
                    StaticClass.proceed = true;
                    log.info("<---- выход из addMedicine()");
                    return message;
            }
        }

        Medicine newMed;

        switch (status.getAddStatus()) {
            case NAME:
                log.info("case NAME");
                newMed = new Medicine();
                newMed.setName(textFromChat);
                message.setText("Введите дозировку лекарства:");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForSkip());
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.DOSAGE)
                        .editStatus(EditStatus.NONE)
                        .medicine(newMed)
                        .build());
                log.info("Блок case NAME:. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case NAME. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case DOSAGE:
                log.info("case DOSAGE");
                newMed = status.getMedicine();
                if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("SKIP_BUTTON")) {
                    message.setText("Введите количество лекарства:");
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.ADD)
                            .addStatus(AddStatus.QUANTITY)
                            .editStatus(EditStatus.NONE)
                            .medicine(newMed)
                            .build());
                    newMed.setDosage("---");
                    break;
                }
                newMed.setDosage(textFromChat);
                message.setText("В чём измерять дозировку лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForDosage());
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.DOSAGE_TYPE)
                        .editStatus(EditStatus.NONE)
                        .medicine(newMed)
                        .build());
                log.info("Блок case DOSAGE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case DOSAGE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case DOSAGE_TYPE:
                log.info("case DOSAGE_TYPE");
                newMed = status.getMedicine();
                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "MG_BUTTON":
                            log.info("MG_BUTTON");
                            newMed.setDosage(newMed.getDosage() + " мг.");
                            break;
                        case "ML_BUTTON":
                            log.info("ML_BUTTON");
                            newMed.setDosage(newMed.getDosage() + " мл.");
                            break;
                        case "SMT_BUTTON":
                            log.info("SMT_BUTTON");
                            newMed.setDosage(newMed.getDosage() + " ед.");
                            break;
                    }
                }
                message.setText("Введите количество лекарства:");
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.QUANTITY)
                        .editStatus(EditStatus.NONE)
                        .medicine(newMed)
                        .build());
                log.info("Блок case DOSAGE_TYPE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case DOSAGE_TYPE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case QUANTITY:
                log.info("case QUANTITY");
                newMed = status.getMedicine();
                newMed.setQuantity(textFromChat);
                message.setText("В чём измерять кол-во лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForQuantity());
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.QUANTITY_TYPE)
                        .editStatus(EditStatus.NONE)
                        .medicine(newMed)
                        .build());
                log.info("Блок case QUANTITY. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case QUANTITY. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case QUANTITY_TYPE:
                log.info("case QUANTITY_TYPE");
                newMed = status.getMedicine();
                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "PILLS_BUTTON":
                            log.info("PILLS_BUTTON");
                            newMed.setQuantity(newMed.getQuantity() + " шт.");
                            break;
                        case "PERCENT_BUTTON":
                            log.info("PERCENT_BUTTON");
                            newMed.setQuantity(newMed.getQuantity() + " %");
                            break;
                    }
                } else {
                    newMed.setQuantity(newMed.getQuantity() + " шт.");
                }
                message.setText("Введите ГОД и МЕСЯЦ через пробел, для указания срока годности:");
                userStatusService.changeCurrentStatus(userId, Status.builder()
                        .mainStatus(MainStatus.ADD)
                        .addStatus(AddStatus.EXP_DATE)
                        .editStatus(EditStatus.NONE)
                        .medicine(newMed)
                        .build());
                log.info("Блок case QUANTITY_TYPE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case QUANTITY_TYPE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case EXP_DATE:
                log.info("case EXP_DATE");
                Optional<Date> optionalDate = dateService.StrToDate(textFromChat);
                newMed = status.getMedicine();

                if (optionalDate.isPresent()) {
                    newMed.setExpDate(optionalDate.get());
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.ADD)
                            .addStatus(AddStatus.PHOTO)
                            .editStatus(EditStatus.NONE)
                            .medicine(newMed)
                            .build());
                    message.setText("Добавьте фотографию препарата или нажмите ОТМЕНА, если добавлять фото не нужно:");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForSkip());
                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                    log.debug("Status при неверном вводе даты {}", status);
                }
                log.info("Блок case EXP_DATE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case EXP_DATE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case PHOTO:
                log.info("case PHOTO");
                newMed = status.getMedicine();
                log.info("Блок case PHOTO. Пробуем принять фото");

                if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("SKIP_BUTTON")) {
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    message.setText("Вы добавили:\n\n" +
                            "Препарат..... " + newMed.getName()
                            + "\nДозировка.. " + newMed.getDosage()
                            + "\nКол-во.......... " + newMed.getQuantity()
                            + "\nГоден до....... " + newMed.getTextExpDate()
                            + "\nФото............. не прикреплено");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
                }

                if (update.hasMessage() && update.getMessage().hasPhoto()) {
                    log.info("----> вход в hasPhoto() <----");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    setMedicinePhoto(update, newMed);
                    message.setText("Вы добавили:\n\n" +
                            "Препарат..... " + newMed.getName()
                            + "\nДозировка.. " + newMed.getDosage()
                            + "\nКол-во.......... " + newMed.getQuantity()
                            + "\nГоден до....... " + newMed.getTextExpDate()
                            + "\nФото............. прикреплено");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
                    log.info("<---- выход из hasPhoto() ---->");
                }

                if (getMedById(newMed) == null) {
                    medicineRepository.save(newMed);
                    log.info("В базу сохранено лекарство: {}", newMed);
                } else {
                    message.setText(String.format("%s\n%s\n%s\n\nУже есть в базе!\nЕсли вы ходите изменить кол-во имеющегося лекарства, то выберите пункт меню ИЗМЕНИТЬ", newMed.getName(), newMed.getDosage(), newMed.getExpDate().toString()));
                }

//                userStatusService.resetStatus(userId);
                break;
        }
        log.info("<---- выход из addMedicine()");
        return message;
    }

    private SendMessage getDetailsByNumber(Update update) {
        log.debug("----> вход в getDetailsByNumber()");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        Long chatId = updateService.getChatId(update);
        message.setChatId(chatId);
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine medicine;

        if (textFromChat == null) {
            medicine = status.getMedicine();
        } else {
            medicine = getMedByNumber(textFromChat, status.getComparator());
        }

        if (medicine == null) {
            message.setText(String.format("В базе нет лекарства с порядковым номером [%s]\nВведите корректный номер:", textFromChat));
            return message;
        }

        message.setText("Препарат..... " + medicine.getName()
                + "\nДозировка.. " + medicine.getDosage()
                + "\nКол-во.......... " + medicine.getQuantity()
                + "\nГоден до....... " + medicine.getTextExpDate());
        message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForDetailView());

        userStatusService.changeCurrentStatus(userId, Status.builder()
                .mainStatus(MainStatus.DETAIL)
                .addStatus(AddStatus.NONE)
                .editStatus(EditStatus.NONE)
                .medicine(medicine)
                .build());

        log.debug("<---- выход из getDetailsByNumber()");
        return message;
    }

    private Medicine getMedByNumber(String medNumberFromUpdateText, Comparator<Medicine> comparator) {
        log.debug("----> вход в getMedByNumber()");
        Medicine medicine;
        List<Medicine> meds = getAllMeds(comparator);

        for (int x = 1; x <= meds.size(); x++) {
            log.info("X = {}, textFromChat = {}", x, medNumberFromUpdateText);
            if (String.valueOf(x).equals(medNumberFromUpdateText)) {
                medicine = getAllMeds(comparator).get(x - 1);
                log.debug("<---- выход из getMedByNumber(): {}", medicine);
                return medicine;
            }
        }
        log.debug("<---- выход из getMedByNumber(): ничего не нашли null");
        return null;
    }

    private boolean edit(Medicine medToEdit, Medicine storedMed) {
        log.debug("----> вход в edit(): попытка заменить {} на {}", medToEdit, storedMed);
        Medicine tempMedicine = medicineRepository.getByNameAndDosageAndExpDate(storedMed.getName(), storedMed.getDosage(), storedMed.getExpDate());
        if (tempMedicine != null) {
            medicineRepository.deleteByNameAndDosageAndExpDate(storedMed.getName(), storedMed.getDosage(), storedMed.getExpDate());
            medicineRepository.save(medToEdit);
            log.debug("<---- выход из edit(): отредактировано");
            return true;
        } else {
            log.debug("<---- выход из edit(): НЕ отредактировано");
            return false;
        }
    }

    private Medicine getMedById(Medicine medicine) {
        log.trace("----> вход в getMedById(): {}", medicine.getName() + " " + medicine.getDosage() + " " + medicine.getExpDate());
        Medicine toOut = medicineRepository.getByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
        log.trace("<---- выход из getMedById(): {}", toOut);
        return toOut;
    }

    private String extractDataWithoutUnits(String withUnits) {
        StringBuilder dosage = new StringBuilder();

        if (withUnits.equals("---")) {
            return dosage.append(withUnits).toString();
        }

        for (int x = 0; withUnits.charAt(x) != ' '; x++) {
            dosage.append(withUnits.charAt(x));
        }
        return dosage.toString();
    }

    private String extractUnitsWithoutData(String withData) {
        StringBuilder dosage = new StringBuilder();

        for (int x = withData.length() - 1; withData.charAt(x) != ' '; x--) {
            dosage.append(withData.charAt(x));
        }
        return dosage.reverse().toString();
    }

    @SneakyThrows
    private void setMedicinePhoto(Update update, Medicine medicine) {
        GetFile getFile = new GetFile();
        getFile.setFileId(update.getMessage().getPhoto().get(3).getFileId());
        File file = remedyBot.execute(getFile);
        remedyBot.downloadFile(file, new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg"));
    }

    private void deleteMedicinePhoto(Medicine medicine) {
        java.io.File fileToDel = new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg");
        fileToDel.delete();
    }

    private void renamePhotoFile(Medicine medBefore, Medicine medAfter) {
        java.io.File file = new java.io.File("./src/main/resources/photo/" + medBefore.getName() + "_" + medBefore.getDosage() + ".jpg");
        file.renameTo(new java.io.File("./src/main/resources/photo/" + medAfter.getName() + "_" + medAfter.getDosage() + ".jpg"));
    }
}
