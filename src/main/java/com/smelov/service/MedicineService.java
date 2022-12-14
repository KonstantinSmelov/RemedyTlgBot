package com.smelov.service;

import com.smelov.entity.Medicine;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMeds(Comparator<Medicine> comparator);
    SendMessage addMedicine(Update update);
    SendMessage deleteMedByNumber(Update update);
    SendMessage editMedByNumber(Update update);
    SendMessage getMedDetails(Update update);
    SendPhoto getMedicinePhoto(Medicine medicine);
}
