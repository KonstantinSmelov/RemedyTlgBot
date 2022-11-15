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
import com.smelov.service.TextMessageService;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicineServiceImpl implements MedicineService {

    public final String CHANGE_TO = "Заменить [%s] на:";
    public final String CHANGE_TO_FOR_DATE = "Заменить [%s] на (введите ГОД и МЕСЯЦ через пробел):";
    public final String NOT_APPLY_FOR_THIS_MEDICINE = "Для данного препарата дозировка не применяется";
    public final String PHOTO_NOT_ASSIGNED = "Фото для [%s] ранее не было назначено!\nСделайте фото:";
    public final String TAKE_A_PHOTO = "Сделайте новое фото для [%s]:";
    public final String SET_NUMBER_FOR_EDIT = "Введите порядковый номер лекарства для редактирования:";
    public final String CANCEL = "Отмена...";
    public final String RETURN = "Возврат...";
    public final String CHANGE = "%s изменено на %s";
    public final String RETURN_TO_MAIN_MENU = "Выходим в главное меню...";
    public final String NOT_EXIST_INTO_DB_FOR_CHANGE = "В базе нет лекарства с порядковым номером [%s].\nВведите корректный номер для внесения изменений:";
    public final String NAME_IS_NOT_CHANGED = "Название НЕ изменено";
    public final String DOSAGE_IS_CHANGED = "Дозировка изменена";
    public final String DOSAGE_IS_NOT_CHANGED = "Дозировка НЕ изменена";
    public final String QTY_IS_CHANGED = "Количество изменено";
    public final String QTY_IS_NOT_CHANGED = "Количество НЕ изменено";
    public final String DATE_IS_CHANGED = "Срок годности изменён";
    public final String PHOTO_IS_CHANGED = "Фото изменено";
    public final String DATE_IS_NOT_CHANGED = "Срок годности НЕ изменён";
    public final String SET_CORRECT_YEAR_AND_MONTH = "Введите ГОД и МЕСЯЦ через пробел";
    public final String SET_DOSAGE = "Введите дозировку лекарства:";
    public final String SET_QTY = "Введите количество лекарства:";
    public final String HOW_TO_MEASURE_DOSAGE = "В чём измерять дозировку лекарства?";
    public final String MG = " мг.";
    public final String ML = " мл.";
    public final String PCS = " ед.";
    public final String PCT = " %";
    public final String HOW_TO_MEASURE_QTY = "В чём измерять кол-во лекарства?";
    public final String NOT_EXIST_INTO_DB_FOR_DELETE = "В базе нет лекарства с порядковым номером [%s].\nВведите корректный номер для удаления:";
    public final String DO_NOT_UNDERSTAND_ADD_MEDICINE = "Простите, не понял в addMedicine";
    public final String TAKE_A_PHOTO_OR_CANCEL = "Добавьте фотографию препарата или нажмите ОТМЕНА, если добавлять фото не нужно:";
    public final String SET_YEAR_AND_MONTH = "Введите ГОД и МЕСЯЦ через пробел";
    public final String NOT_EXIST_INTO_DB_FOR_DETAIL = "В базе нет лекарства с порядковым номером [%s].\nВведите корректный номер для показа деталей:";
    public final String ALREADY_EXIST_IN_DB = "%s\n%s\n%s\n\nУже есть в базе!\nЕсли вы ходите изменить кол-во имеющегося лекарства, то выберите пункт меню ИЗМЕНИТЬ";

    public final String PATH_FOR_PHOTO = "./src/main/resources/photo/";
    public final String FILE_EXTENSION = ".jpg";

    private final MedicineRepository medicineRepository;
    private final UserStatusServiceImpl userStatusService;
    private final CustomInlineKeyboardMarkup customInlineKeyboardMarkup;
    private final UpdateService updateService;
    private final DateService dateService;
    private final RemedyBot remedyBot;
    private final ChatMessagesService chatMessagesService;
    private final TextMessageService textMessageService;

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
                    message.setText(String.format(CHANGE_TO, medicine.getName()));
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
                        message.setText(NOT_APPLY_FOR_THIS_MEDICINE);
                        userStatusService.changeCurrentStatus(userId, Status.builder()
                                .mainStatus(MainStatus.EDIT)
                                .addStatus(AddStatus.NONE)
                                .editStatus(EditStatus.NONE)
                                .comparator(status.getComparator())
                                .medicine(medicine)
                                .build());
                    } else {
                        message.setText(String.format(CHANGE_TO, extractDataWithoutUnits(medicine.getDosage())));
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
                    message.setText(String.format(CHANGE_TO, extractDataWithoutUnits(medicine.getQuantity())));
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
                    message.setText(String.format(CHANGE_TO_FOR_DATE, medicine.getTextExpDate()));
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
                        message.setText(String.format(PHOTO_NOT_ASSIGNED, medicine.getName() +
                                ((medicine.getDosage().equals("---"))
                                        ? ""
                                        : (" - " + medicine.getDosage()))));
                    } else {
                        message.setText(String.format(TAKE_A_PHOTO, medicine.getName() +
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
                    message.setText(SET_NUMBER_FOR_EDIT);
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
                            .build());
                    message.setText(RETURN_TO_MAIN_MENU);
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
                    message.setText(RETURN);
                    StaticClass.proceed = true;
                    update.getCallbackQuery().setData("");
                    log.info("<---- выход из editMedByNumber()");
                    return message;

                case "CANCEL_BUTTON":
                    log.info("CANCEL_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText(CANCEL);
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
                    status.setMedicine(medicine);
                    message.setText("Препарат..... " + medicine.getName()
                            + "\nДозировка.. " + medicine.getDosage()
                            + "\nКол-во.......... " + medicine.getQuantity()
                            + "\nГоден до....... " + medicine.getTextExpDate()
                            + "\n\nЧто меняем?");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                } else {
                    chatMessagesService.showMedsNameList(chatId, userId, textMessageService.nameList(getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
                    message.setText(String.format(NOT_EXIST_INTO_DB_FOR_CHANGE, textFromChat));
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
                    message.setText(String.format(CHANGE, medicine.getName(), medToEdit.getName()));
                } else {
                    message.setText(NAME_IS_NOT_CHANGED);
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
                    message.setText(DOSAGE_IS_CHANGED);
                } else {
                    message.setText(DOSAGE_IS_NOT_CHANGED);
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
                    message.setText(QTY_IS_CHANGED);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
                } else {
                    message.setText(QTY_IS_NOT_CHANGED);
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
                        message.setText(DATE_IS_CHANGED);
                    } else {
                        message.setText(DATE_IS_NOT_CHANGED);
                    }
                } else {
                    message.setText(SET_CORRECT_YEAR_AND_MONTH);
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
                message.setText(PHOTO_IS_CHANGED);
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
    public SendMessage getMedDetails(Update update) {
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

                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    message = getDetailsByNumber(update);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());

                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.EDIT)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .medicine(status.getMedicine())
                            .comparator(status.getComparator())
                            .build());
                    return message;

                case "MAIN_MENU_BUTTON":
                    log.info("MAIN_MENU_BUTTON");
                    userStatusService.changeCurrentStatus(userId, Status.builder()
                            .mainStatus(MainStatus.MAIN_MENU)
                            .addStatus(AddStatus.NONE)
                            .editStatus(EditStatus.NONE)
                            .comparator(Comparator.comparing(Medicine::getName))
                            .build());
                    message.setText(RETURN_TO_MAIN_MENU);
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
        java.io.File file = new java.io.File(PATH_FOR_PHOTO + medicine.getName() + "_" + medicine.getDosage() + FILE_EXTENSION);
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
                            .userMessageIds(new LinkedHashSet<>())
                            .build());
                    message.setText(RETURN_TO_MAIN_MENU);
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
            message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForMainMenu());
        } else {
            chatMessagesService.showMedsNameList(chatId, userId, textMessageService.nameList(getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
            message.setText(String.format(NOT_EXIST_INTO_DB_FOR_DELETE, textFromChat));
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
        message.setText(DO_NOT_UNDERSTAND_ADD_MEDICINE);

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
                            .userMessageIds(new LinkedHashSet<>())
                            .build());
                    message.setText(RETURN_TO_MAIN_MENU);
                    StaticClass.proceed = true;
                    log.info("<---- выход из addMedicine()");
                    return message;

                case "CANCEL_BUTTON":
                    log.info("case CANCEL_BUTTON");
                    chatMessagesService.deleteMessagesFromChat(chatId, userId);
                    userStatusService.resetStatus(chatId, userId);
                    message.setText(CANCEL);
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
                message.setText(SET_DOSAGE);
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
                    message.setText(SET_QTY);
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
                message.setText(HOW_TO_MEASURE_DOSAGE);
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

                if (update.hasMessage()) {
                    message.setText(HOW_TO_MEASURE_DOSAGE);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForDosage());
                    chatMessagesService.deleteLastMessagesFromChat(chatId, userId, 2);
                }

                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "MG_BUTTON":
                            log.info("MG_BUTTON");
                            newMed.setDosage(newMed.getDosage() + MG);
                            break;
                        case "ML_BUTTON":
                            log.info("ML_BUTTON");
                            newMed.setDosage(newMed.getDosage() + ML);
                            break;
                        case "SMT_BUTTON":
                            log.info("SMT_BUTTON");
                            newMed.setDosage(newMed.getDosage() + PCS);
                            break;
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
                }
                break;

            case QUANTITY:
                log.info("case QUANTITY");
                newMed = status.getMedicine();
                newMed.setQuantity(textFromChat);
                message.setText(HOW_TO_MEASURE_QTY);
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

                if (update.hasMessage()) {
                    message.setText(HOW_TO_MEASURE_QTY);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForQuantity());
                    chatMessagesService.deleteLastMessagesFromChat(chatId, userId, 2);
                }

                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "PILLS_BUTTON":
                            log.info("PILLS_BUTTON");
                            newMed.setQuantity(newMed.getQuantity() + PCS);
                            break;
                        case "PERCENT_BUTTON":
                            log.info("PERCENT_BUTTON");
                            newMed.setQuantity(newMed.getQuantity() + PCT);
                            break;
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
                }

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
                    message.setText(TAKE_A_PHOTO_OR_CANCEL);
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForSkip());
                } else {
                    message.setText(SET_YEAR_AND_MONTH);
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
                }
                message.setText(String.format(ALREADY_EXIST_IN_DB, newMed.getName(), newMed.getDosage(), newMed.getExpDate().toString()));
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
            chatMessagesService.showMedsNameList(chatId, userId, textMessageService.nameList(getAllMeds(userStatusService.getCurrentStatus(userId).getComparator())));
            message.setText(String.format(NOT_EXIST_INTO_DB_FOR_DETAIL, textFromChat));
            return message;
        }

        LocalDate now = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);

        String text = "Препарат..... " +
                medicine.getName() +
                "\nДозировка.. " +
                medicine.getDosage() +
                "\nКол-во.......... " +
                medicine.getQuantity() +
                "\nГоден до....... " +
                medicine.getTextExpDate();

        if(now.compareTo(medicine.getExpDate().toLocalDate()) > 0) {
            text = text + " <i>[истёк!!!]</i>";
        } else if (now.plus(1, ChronoUnit.MONTHS).compareTo(medicine.getExpDate().toLocalDate()) == 0
                || now.compareTo(medicine.getExpDate().toLocalDate()) == 0) {
            text = text + "  <i>[истекает!]</i>";
        }

        message.enableHtml(true);
        message.setText(text);
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
        remedyBot.downloadFile(file, new java.io.File( PATH_FOR_PHOTO+ medicine.getName() + "_" + medicine.getDosage() + FILE_EXTENSION));
    }

    private void deleteMedicinePhoto(Medicine medicine) {
        java.io.File fileToDel = new java.io.File(PATH_FOR_PHOTO + medicine.getName() + "_" + medicine.getDosage() + FILE_EXTENSION);
        fileToDel.delete();
    }

    private void renamePhotoFile(Medicine medBefore, Medicine medAfter) {
        java.io.File file = new java.io.File(PATH_FOR_PHOTO + medBefore.getName() + "_" + medBefore.getDosage() + FILE_EXTENSION);
        file.renameTo(new java.io.File(PATH_FOR_PHOTO + medAfter.getName() + "_" + medAfter.getDosage() + FILE_EXTENSION));
    }
}
