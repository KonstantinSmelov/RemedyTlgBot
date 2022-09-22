package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.service.TextMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TextMessageServiceImpl implements TextMessageService {

    @Override
    public String medNameAndDosageToText(List<Medicine> meds) {

        log.info("----> вход в medNameAndDosageToText() <----");
        StringBuilder string = new StringBuilder();
        int x = 1;

        for (Medicine med : meds) {
            string
                    .append(x++)
                    .append(" - ")
                    .append(med.getName())
                    .append(" - ")
                    .append(med.getDosage())
                    .append("\n");
        }

        if(string.length() == 0) {
            string.append("ПУСТО");
            log.info("<---- выход из medNameAndDosageToText() ---->");
            return string.toString();
        }

        log.info("<---- выход из medNameAndDosageToText() ---->");
        return string.toString();
    }

    @Override
    public String allMedInfoToText(List<Medicine> meds) {

        log.info("----> вход в allMedInfoToText() <----");
        StringBuilder string = new StringBuilder();
        int x = 1;
        for (Medicine med : meds) {
            string
                    .append(x++)
                    .append(" - ")
                    .append(med.getName())
                    .append(" - ")
                    .append(med.getDosage())
                    .append(" - ")
                    .append(med.getQuantity())
                    .append(" - ")
                    .append(med.getExpDate())
                    .append("\n");
        }

        if(string.length() == 0) {
            log.info("<---- выход из allMedInfoToText() ---->");
            return string.append("ПУСТО").toString();
        }

        log.info("<---- выход из allMedInfoToText() ---->");
        return string.toString();
    }
}
