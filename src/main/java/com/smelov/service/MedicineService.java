package com.smelov.service;

import com.smelov.entity.Medicine;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMeds();
    List<Medicine> getMedsByName(String name);
    void save(Medicine medicine);
    SendMessage addMedicine(Update update);
    Medicine getMedById (Medicine medicine); //ID состоит из name, dosage и expDate
    SendMessage deleteMedByNumber(Update update);
}
