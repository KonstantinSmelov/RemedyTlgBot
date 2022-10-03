package com.smelov.service;

import com.smelov.entity.Medicine;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMeds();
    void save(Medicine medicine);
    boolean edit(Medicine medToEdit, Medicine storedMed);
    SendMessage addMedicine(Update update);
    Medicine getMedById (Medicine medicine);
    SendMessage deleteMedByNumber(Update update);
    SendMessage editMedByNumber(Update update);
}
