package com.smelov.service;

import com.smelov.entity.Medicine;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMeds(Comparator<Medicine> comparator);
    void save(Medicine medicine);
    boolean edit(Medicine medToEdit, Medicine storedMed);
    SendMessage addMedicine(Update update);
//    SendMessage detailMedicine(Update update, SendPhoto photo, Comparator<Medicine> comparator);
    Medicine getMedById (Medicine medicine);
    SendMessage deleteMedByNumber(Update update, Comparator<Medicine> comparator);
    SendMessage editMedByNumber(Update update, Comparator<Medicine> comparator);
    SendPhoto getMedicinePhoto(Medicine medicine);
    Medicine getMedByNumber(Update update, Comparator<Medicine> comparator);
}
