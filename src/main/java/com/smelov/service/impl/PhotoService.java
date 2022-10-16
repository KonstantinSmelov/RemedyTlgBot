package com.smelov.service.impl;

import com.smelov.StaticClass;
import com.smelov.bot.RemedyBot;
import com.smelov.entity.Medicine;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final MedicineService medicineService;
    private final UpdateService updateService;
    private final RemedyBot remedyBot;

    @SneakyThrows
    public void showPhotoIfExist(Update update, Medicine medicine) {
        if(medicine != null && medicineService.getMedicinePhoto(medicine) != null) {
            SendPhoto photo = medicineService.getMedicinePhoto(medicine);
            photo.setChatId(updateService.getChatId(update));
            Message sended = remedyBot.execute(photo);
            StaticClass.userMessageIds.add(sended.getMessageId());
        }
    }
}
