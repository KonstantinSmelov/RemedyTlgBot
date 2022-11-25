package com.smelov.service.impl;

import com.smelov.bot.RemedyBot;
import com.smelov.entity.Medicine;
import com.smelov.service.MedicineService;
import com.smelov.service.UpdateService;
import com.smelov.service.UserStatusService;
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
    private final ChatMessagesService chatMessagesService;

    @SneakyThrows
    public void showPhotoIfExist(Update update, Medicine medicine) {
        log.info("----> вход в showPhotoIfExist(): {}", medicine);
        if (medicine != null && medicineService.getMedicinePhoto(medicine) != null) {
            SendPhoto photo = medicineService.getMedicinePhoto(medicine);
            photo.setChatId(updateService.getChatId(update));
            Message forDelete = remedyBot.execute(photo);
            chatMessagesService.addNewIdToMessageIds(updateService.getUserId(update), forDelete.getMessageId());
            log.info("<---- выход из showPhotoIfExist(): фото найдено");
        } else {
            log.info("<---- выход из showPhotoIfExist(): фото НЕ найдено");
        }
    }
}
