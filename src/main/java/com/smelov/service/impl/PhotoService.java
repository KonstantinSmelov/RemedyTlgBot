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
    private final UserStatusService userStatusService;

    @SneakyThrows
    public void showPhotoIfExist(Update update, Medicine medicine) {
        log.info("----> showPhotoIfExist(): {}", medicine);
        if (medicine != null && medicineService.getMedicinePhoto(medicine) != null) {
            SendPhoto photo = medicineService.getMedicinePhoto(medicine);
            photo.setChatId(updateService.getChatId(update));
            Message forDelete = remedyBot.execute(photo);
            System.out.println(userStatusService.getCurrentStatus(updateService.getUserId(update)).getUserMessageIds());
            chatMessagesService.addNewIdToMessageIds(forDelete.getMessageId(), updateService.getUserId(update));
            log.info("<---- showPhotoIfExist(): фото найдено");
        } else {
            log.info("<---- showPhotoIfExist(): фото НЕ найдено");
        }
    }
}
