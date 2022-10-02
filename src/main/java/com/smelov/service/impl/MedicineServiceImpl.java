package com.smelov.service.impl;

import com.smelov.bot.CustomInlineKeyboardMarkup;
import com.smelov.dao.MedicineRepository;
import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.Status;
import com.smelov.service.DateService;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Medicine> getAllMeds() {
        return medicineRepository.findAll();
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
    public SendMessage editMedByNumber(Update update) {
        log.info("----> вход в editMedByNumber() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        message.setChatId(updateService.getChatId(update));
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine medicine;

        switch (status.getEditStatus()) {
            case NONE:
                log.debug("editMedByNumber(), блок case NONE");
                medicine = getMedByNumber(update);

                if (medicine != null) {
                    status.setMedicine(medicine);
                    message.setText("Выбрано лекарство:\n" + textFromChat + " - " + medicine.getName() +
                            " - " + medicine.getDosage() + " - " + medicine.getQuantity() +
                            " - " + medicine.getExpDate() + "\n\nЧто меняем?");
                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForEdit());
                    userStatusService.setCurrentStatus(userId, Status.EDIT.setEditStatus(EditStatus.EDIT_NAME).setMedicine(medicine));
                    System.out.println(status);
                } else if (status.getEditStatus().equals(EditStatus.NONE)) {
                    message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
//                    message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
                }
                break;

            case EDIT_NAME:
                log.debug("editMedByNumber(), блок case EDIT_NAME");
                if(update.)
                medicine = status.getMedicine();
                medicine.setName("asdfasdfa");
                save(medicine);
                userStatusService.resetStatus(userId);
                message.setText("Название изменено");
                break;
        }
        log.info("<---- выход из editMedByNumber() ---->");
        return message;
    }

    @Override
    public SendMessage deleteMedByNumber(Update update) {
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

        medicine = getMedByNumber(update);

        if (medicine != null) {
            log.debug("deleteMedByNumber(): Нашли лекарство {}", medicine);
            medicineRepository.deleteByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
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
    public SendMessage addMedicine(Update update) {
        log.info("----> вход в addMedicine() <----");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Status status = userStatusService.getCurrentStatus(userId);
        Medicine newMed;

        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял, начните с начала в addMedicine");

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
                    if (getMedById(newMed) == null) {
                        medicineRepository.save(newMed);
                        log.info("В базу сохранено лекарство: {}", newMed);
                        message.setText(String.format("Вы добавили %s в базу", newMed.getName()));
                    } else {
                        message.setText(String.format("%s\n%s\n%s\n\nУже есть в базе!\nЕсли вы ходите изменить кол-во имеющегося лекарства, то выберите пункт меню редактировать", newMed.getName(), newMed.getDosage(), newMed.getExpDate().toString()));
                    }
//                    userStatusService.setCurrentStatus(userId, null);
                    userStatusService.resetStatus(userId);

                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                    log.debug("Status при неверном вводе даты {}", status);
                }
                break;
        }
        log.info("<---- выход из addMedicine() ---->");
        return message;
    }

    private Medicine getMedByNumber(Update update) {
        String textFromChat = updateService.getTextFromMessage(update);
        Medicine medicine = new Medicine();

        for (int x = 1; x <= getAllMeds().size(); x++) {
            log.info("X = {}, textFromChat = {}", x, textFromChat);
            if (String.valueOf(x).equals(textFromChat)) {
                medicine = getAllMeds().get(x - 1);
                log.debug("getMedByNumber(): Нашли лекарство {}", medicine);
                return medicine;
            }
        }
        return null;
    }

}
