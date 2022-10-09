package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.keyboard.CustomInlineKeyboardMarkup;
import com.smelov.dao.MedicineRepository;
import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.Status;
import com.smelov.service.DateService;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Date;
import java.util.Comparator;
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

    @Override
    public List<Medicine> getAllMeds(Comparator<Medicine> comparator) {
        log.debug("----> вход в getAllMeds() <----");
        List<Medicine> meds = medicineRepository.findAll();
        log.debug("getAllMeds(): получили список из БД: {}", meds.stream().map(Medicine::getName).collect(Collectors.toList()));
        meds.sort(comparator);
        log.debug("getAllMeds(): отсортировали список: {}", meds.stream().map(Medicine::getName).collect(Collectors.toList()));
        log.debug("<---- выход из getAllMeds() ---->");
        return meds;
    }

    @Override
    public Medicine getMedById(Medicine medicine) {
        return medicineRepository.getByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
    }

    @Override
    public void save(Medicine medicine) {
        medicineRepository.save(medicine);
    }

    @Override
    public boolean edit(Medicine medToEdit, Medicine storedMed) {
        log.info("----> вход в edit() <----");
        Medicine tempMedicine = medicineRepository.getByNameAndDosageAndExpDate(storedMed.getName(), storedMed.getDosage(), storedMed.getExpDate());
        if (tempMedicine != null) {
            medicineRepository.deleteByNameAndDosageAndExpDate(storedMed.getName(), storedMed.getDosage(), storedMed.getExpDate());
            medicineRepository.save(medToEdit);
            log.info("<---- выход из edit() ---->");
            return true;
        } else {
            log.info("<---- выход из edit() ---->");
            return false;
        }
    }

    @Override
    public SendMessage editMedByNumber(Update update, Comparator<Medicine> comparator) {
        log.info("----> вход в editMedByNumber() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        message.setChatId(updateService.getChatId(update));
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine medicine = status.getMedicine();
        Medicine medToEdit;

        if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case "EDIT_NAME_BUTTON":
                    log.info("EDIT_NAME_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на:", medicine.getName()));
                    userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_NAME).setMedicine(medicine));
                    log.info("<---- выход из editMedByNumber() ---->");
                    return message;

                case "EDIT_DOSAGE_BUTTON":
                    log.info("EDIT_DOSAGE_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на:", extractDataWithoutUnits(medicine.getDosage())));
                    userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_DOSAGE).setMedicine(medicine));
                    log.info("<---- выход из editMedByNumber() ---->");
                    return message;

                case "EDIT_QUANTITY_BUTTON":
                    log.info("EDIT_QUANTITY_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на:", extractDataWithoutUnits(medicine.getQuantity())));
                    userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_QTY).setMedicine(medicine));
                    log.info("<---- выход из editMedByNumber() ---->");
                    return message;

                case "EDIT_EXP_DATE_BUTTON":
                    log.info("EDIT_EXP_DATE_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    message.setText(String.format("Заменить [%s] на (введите год и месяц через пробел):", medicine.getTextExpDate()));
                    userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_EXP).setMedicine(medicine));
                    log.info("<---- выход из editMedByNumber() ---->");
                    return message;

                case "EDIT_PHOTO_BUTTON":
                    log.info("EDIT_PHOTO_BUTTON");
                    message.setReplyMarkup(new ReplyKeyboardRemove(true));
                    SendPhoto photo = getMedicinePhoto(medicine);
                    if ((photo == null)) {
                        message.setText(String.format("Фото для [%s] не назначено!", medicine.getName() + " - " + medicine.getDosage()));
                        userStatusService.resetStatus(userId);
                    } else {
                        message.setText(String.format("Сделайте новое фото для [%s]", medicine.getName() + " - " + medicine.getDosage()));
                        userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_PHOTO).setMedicine(medicine));
                    }
                    return message;
            }
        }

        switch (status.getEditStatus()) {
            case NONE:
                log.debug("editMedByNumber(), блок case NONE");
                medicine = getMedByNumber(update, comparator);
                if (medicine != null) {
                    status.setMedicine(medicine);
                    message.setText("Выбрано лекарство:\n" + textFromChat + " - " + medicine.getName() +
                            " - " + medicine.getDosage() + " - " + medicine.getQuantity() +
                            " - " + medicine.getExpDate() + "\n\nЧто меняем?");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                } else {
                    message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
                }
                break;

            case EDIT_NAME:
                log.debug("editMedByNumber(), блок case EDIT_NAME");
                medToEdit = new Medicine(medicine);
                medToEdit.setName(textFromChat);
                if (edit(medToEdit, medicine)) {
                    userStatusService.resetStatus(userId);
                    message.setText("Название изменено");
                } else {
                    message.setText("Название НЕ изменено");
                }
                break;

            case EDIT_DOSAGE:
                log.debug("editMedByNumber(), блок case EDIT_DOSAGE");
                medToEdit = new Medicine(medicine);
                medToEdit.setDosage(textFromChat + medicine.getDosage().substring(medicine.getDosage().length() - 4));
                if (edit(medToEdit, medicine)) {
                    userStatusService.resetStatus(userId);
                    message.setText("Дозировка изменена");
                } else {
                    message.setText("Дозировка НЕ изменена");
                }
                break;

            case EDIT_QTY:
                log.debug("editMedByNumber(), блок case EDIT_QTY");
                medToEdit = new Medicine(medicine);
                medToEdit.setQuantity(textFromChat + " " + extractUnitsWithoutData(medicine.getQuantity()));
                if (edit(medToEdit, medicine)) {
                    userStatusService.resetStatus(userId);
                    message.setText("Кол-во изменено");
                } else {
                    message.setText("Кол-во НЕ изменено");
                }
                break;

            case EDIT_EXP:
                log.debug("editMedByNumber(), блок case EDIT_QTY");
                medToEdit = new Medicine(medicine);
                Optional<Date> optionalDate = dateService.StrToDate(textFromChat);
                if (optionalDate.isPresent()) {
                    medToEdit.setExpDate(optionalDate.get());
                    if (edit(medToEdit, medicine)) {
                        userStatusService.resetStatus(userId);
                        message.setText("Срок годности изменён");
                    } else {
                        message.setText("Срок годности НЕ изменён");
                    }
                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                }
                break;

            case EDIT_PHOTO:
                log.debug("editMedByNumber(), блок case EDIT_PHOTO");

                deleteMedicinePhoto(medicine);
                setMedicinePhoto(update, medicine);

                userStatusService.resetStatus(userId);
                message.setText("Фото изменено");

                break;
        }
        log.info("<---- выход из editMedByNumber() ---->");
        return message;
    }

    @Override
    public SendMessage deleteMedByNumber(Update update, Comparator<Medicine> comparator) {
        log.info("----> вход в deleteMedByNumber() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Medicine medicine;

        message.setChatId(updateService.getChatId(update));

//        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("CANCEL_BUTTON")) {
//            log.debug("CANCEL_BUTTON");
//            message.setText("Отмена удаления. Выход в главное меню");
//            message.setReplyMarkup(new ReplyKeyboardRemove(true));
//            userStatusService.resetStatus(userId);
//            return message;
//        }

        medicine = getMedByNumber(update, comparator);

        if (medicine != null) {
            log.debug("deleteMedByNumber(): Нашли лекарство {}", medicine);
            medicineRepository.deleteByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
            deleteMedicinePhoto(medicine);
            message.setText("Вы удалили лекарство:\n" + textFromChat + " - " + medicine.getName() + " - " + medicine.getDosage() + " - " + medicine.getQuantity() + " - " + medicine.getExpDate());
            userStatusService.resetStatus(userId);
            message.setReplyMarkup(new ReplyKeyboardRemove(true));
        } else {
            message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
//            message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
        }

        log.info("<---- выход из deleteMedByNumber() ---->");
        return message;
    }

    @Override
    @SneakyThrows
    public SendMessage addMedicine(Update update) {
        log.info("----> вход в addMedicine() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine newMed;

        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял, начните с начала (в addMedicine)");

        switch (status.getAddStatus()) {
            case NAME:
                newMed = new Medicine();
                newMed.setName(textFromChat);
                message.setText("Введите дозировку лекарства");
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.DOSAGE).setMedicine(newMed));
                log.info("Блок case NAME:. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case NAME. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case DOSAGE:
                newMed = status.getMedicine();
                newMed.setDosage(textFromChat);
                message.setText("В чём измерять дозировку лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForDosage());
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.DOSAGE_TYPE).setMedicine(newMed));
                log.info("Блок case DOSAGE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case DOSAGE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case DOSAGE_TYPE:
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
                    }
                } else {
                    newMed.setDosage(newMed.getDosage() + " мг.");
                }
                message.setText("Введите количество лекарства");
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.QUANTITY).setMedicine(newMed));
                log.info("Блок case DOSAGE_TYPE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case DOSAGE_TYPE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case QUANTITY:
                newMed = status.getMedicine();
                newMed.setQuantity(textFromChat);
                message.setText("В чём измерять кол-во лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForQuantity());
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.QUANTITY_TYPE).setMedicine(newMed));
                log.info("Блок case QUANTITY. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case QUANTITY. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case QUANTITY_TYPE:
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
                message.setText("Введите срок годности. Год и месяц через пробел:");
                userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.EXP_DATE).setMedicine(newMed));
                log.info("Блок case QUANTITY_TYPE. Добавили в Map: key:{}  value:{}", userId, status);
                log.debug("Блок case QUANTITY_TYPE. Map содержит:\n{}", userStatusService.getStatusMap());
                break;

            case EXP_DATE:
                Optional<Date> optionalDate = dateService.StrToDate(textFromChat);
                newMed = status.getMedicine();

                if (optionalDate.isPresent()) {
                    newMed.setExpDate(optionalDate.get());
                    userStatusService.setCurrentStatus(userId, Status.ADD.setAddStatus(AddStatus.PHOTO).setMedicine(newMed));
                    message.setText("Добавьте фотографию препарата или нажмите ОТМЕНА, если добавлять фото не нужно:");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                    log.debug("Status при неверном вводе даты {}", status);
                }
                break;

            case PHOTO:
                newMed = status.getMedicine();
                log.info("Блок case PHOTO. Пробуем принять фото");

                if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("CANCEL_BUTTON")) {
                    message.setText(String.format("Вы добавили %s в базу без фото", newMed.getName()));
//                    message.setReplyMarkup(new ReplyKeyboardRemove());
                }

                if (update.hasMessage() && update.getMessage().hasPhoto()) {
                    log.info("----> вход в hasPhoto() <----");
                    setMedicinePhoto(update, newMed);
//                    remedyBot.execute(message);
                    message.setText(String.format("Вы добавили %s в базу с фото", newMed.getName()));
//                    message.setReplyMarkup(new ReplyKeyboardRemove());
                    log.info("<---- выход из hasPhoto() ---->");
                }

                if (getMedById(newMed) == null) {
                    medicineRepository.save(newMed);
                    log.info("В базу сохранено лекарство: {}", newMed);
//                    message.setText(String.format("Вы добавили %s в базу", newMed.getName()));
                } else {
                    message.setText(String.format("%s\n%s\n%s\n\nУже есть в базе!\nЕсли вы ходите изменить кол-во имеющегося лекарства, то выберите пункт меню ИЗМЕНИТЬ", newMed.getName(), newMed.getDosage(), newMed.getExpDate().toString()));
                }

                userStatusService.resetStatus(userId);
                break;
        }
        log.info("<---- выход из addMedicine() ---->");
        return message;
    }

    public Medicine getMedByNumber(Update update, Comparator<Medicine> comparator) {
        String textFromChat = updateService.getTextFromMessage(update);
        Medicine medicine;
        List<Medicine> meds = getAllMeds(comparator);

        for (int x = 1; x <= meds.size(); x++) {
            log.info("X = {}, textFromChat = {}", x, textFromChat);
            if (String.valueOf(x).equals(textFromChat)) {
                medicine = getAllMeds(comparator).get(x - 1);
                log.debug("getMedByNumber(): Нашли лекарство {}", medicine);
                return medicine;
            }
        }
        return null;
    }

    private String extractDataWithoutUnits(String withUnits) {
        StringBuilder dosage = new StringBuilder();

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
    public SendPhoto getMedicinePhoto(Medicine medicine) {
        SendPhoto photo = null;
        java.io.File file = new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg");
        if (file.exists()) {
            photo = new SendPhoto();
            InputFile inputFile = new InputFile();
            inputFile.setMedia(file);
            photo.setPhoto(inputFile);
        }
        return photo;
    }

    @SneakyThrows
    public void setMedicinePhoto(Update update, Medicine medicine) {
        GetFile getFile = new GetFile();
        getFile.setFileId(update.getMessage().getPhoto().get(3).getFileId());
        File file = remedyBot.execute(getFile);
        remedyBot.downloadFile(file, new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg"));
    }

    private void deleteMedicinePhoto(Medicine medicine) {
        java.io.File fileToDel = new java.io.File("./src/main/resources/photo/" + medicine.getName() + "_" + medicine.getDosage() + ".jpg");
        fileToDel.delete();
    }
}
