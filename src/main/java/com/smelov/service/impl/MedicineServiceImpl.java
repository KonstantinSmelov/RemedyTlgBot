package com.smelov.service.impl;

import com.smelov.bot.CustomInlineKeyboardMarkup;
import com.smelov.dao.MedicineRepository;
import com.smelov.bot.entity.Medicine;
import com.smelov.model.Status;
import com.smelov.service.MedicineService;
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
    private final UpdateServiceImpl updateService;
    private final DateServiceImpl dateService;

    @Override
    public List<Medicine> getAllMeds() {
        return medicineRepository.findAll();
    }

    @Override
    public List<Medicine> getMedsByName(String name) {
        return medicineRepository.findMedicineByName(name);
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
    public SendMessage deleteMedByNumber(Update update) {
        log.info("Вошли в deleteMedByNumber()");
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Medicine medicine = new Medicine();

        message.setChatId(updateService.getChatId(update));

        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("CANCEL_BUTTON")) {
            log.info("CANCEL_BUTTON");
            message.setText("Отмена удаления. Выход в главное меню");
            message.setReplyMarkup(new ReplyKeyboardRemove(true));
            userStatusService.setCurrentStatus(userId, null);
            return message;
        }

        for (int x = 1; x <= getAllMeds().size(); x++) {
            log.info("X = {}, textFromChat = {}", x, textFromChat);
            if (String.valueOf(x).equals(textFromChat)) {
                medicine = getAllMeds().get(x-1);
                log.info("Нашли лекарство {}", medicine);
                medicineRepository.deleteByNameAndDosageAndExpDate(medicine.getName(), medicine.getDosage(), medicine.getExpDate());
                message.setText("Вы удалили лекарство:\n"+ x + " - " +  medicine.getName() + " - " + medicine.getDosage() + " - " + medicine.getQuantity() + " - " + medicine.getExpDate());
                userStatusService.setCurrentStatus(userId, null);
                message.setReplyMarkup(new ReplyKeyboardRemove(true));
                break;
            }
            message.setText(String.format("В базе нет лекарства с порядковым номером %s\nВведите корректный номер:", textFromChat));
            message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForCancel());
        }
        return message;
    }

    @Override
    public SendMessage addMedicine(Update update) {
        SendMessage message = new SendMessage();
        Long userId = updateService.getUserId(update);
        String textFromChat = updateService.getTextFromMessage(update);
        Medicine newMed;

        message.setChatId(updateService.getChatId(update));
        message.setText("Простите, не понял, начните с начала");

        switch (userStatusService.getCurrentStatus(userId)) {

            case NAME:
                newMed = new Medicine();
                newMed.setName(textFromChat);
                message.setText("Введите дозировку лекарства");
                userStatusService.setCurrentStatus(userId, Status.DOSAGE.setMedicine(newMed));
                break;

            case DOSAGE:
                newMed = userStatusService.getCurrentStatus(userId).getMedicine();
                newMed.setDosage(textFromChat);
                message.setText("В чём измерять дозировку лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForDosage());
                userStatusService.setCurrentStatus(userId, Status.DOSAGE_TYPE.setMedicine(newMed));
                break;

            case DOSAGE_TYPE:
                newMed = userStatusService.getCurrentStatus(userId).getMedicine();
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
                userStatusService.setCurrentStatus(userId, Status.QUANTITY.setMedicine(newMed));
                break;

            case QUANTITY:
                newMed = userStatusService.getCurrentStatus(userId).getMedicine();
                newMed.setQuantity(textFromChat);
                message.setText("В чём измерять кол-во лекарства?");
                message.setReplyMarkup(customInlineKeyboardMarkup.inlineKeyboardForQuantity());
                userStatusService.setCurrentStatus(userId, Status.QUANTITY_TYPE.setMedicine(newMed));
                break;

            case QUANTITY_TYPE:
                newMed = userStatusService.getCurrentStatus(userId).getMedicine();
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
                userStatusService.setCurrentStatus(userId, Status.EXP_DATE.setMedicine(newMed));
                break;

            case EXP_DATE:
                Optional<Date> optionalDate = dateService.StrToDate(textFromChat);
                newMed = userStatusService.getCurrentStatus(userId).getMedicine();

                if (optionalDate.isPresent()) {
                    newMed.setExpDate(optionalDate.get());
                    if (getMedById(newMed) == null) {
                        medicineRepository.save(newMed);
                        message.setText(String.format("Вы добавили %s в базу", newMed.getName()));
                    } else {
                        message.setText(String.format("%s\n%s\n%s\n\nУже есть в базе!\nЕсли вы ходите изменить кол-во имеющегося лекарства, то выберите пункт меню редактировать", newMed.getName(), newMed.getDosage(), newMed.getExpDate().toString()));
                    }
                    userStatusService.setCurrentStatus(userId, null);

                } else {
                    message.setText("Введите ГОД и МЕСЯЦ через пробел");
                    log.info("Status при неверном вводе даты {}", userStatusService.getCurrentStatus(userId));
                }
                break;
        }
        return message;
    }
}
