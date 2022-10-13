package com.smelov.service;

import com.smelov.entity.Medicine;
import com.smelov.model.Status;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMeds(Comparator<Medicine> comparator);
    boolean edit(Medicine medToEdit, Medicine storedMed);
    SendMessage addMedicine(Update update);
    SendMessage deleteMedByNumber(Update update, Status status);
    SendMessage editMedByNumber(Update update, Comparator<Medicine> comparator);
    SendMessage getDetailsByNumber(Update update, Status status);
    Medicine getMedByNumber(Update update, Comparator<Medicine> comparator);
}
