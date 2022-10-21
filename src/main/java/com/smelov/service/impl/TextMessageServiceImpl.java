package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.service.TextMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


@Service
@Slf4j
public class TextMessageServiceImpl implements TextMessageService {

    @Override
    public String nameList(List<Medicine> meds) {
        log.info("----> вход в nameList() <----");
        StringBuilder string = new StringBuilder();
        string.append("Препараты в наличии: \n\n");
        int x = 1;

        for (Medicine med : meds) {
            string
                    .append(x++)
                    .append(" - ")
                    .append(med.getName())
                    .append("\n");
        }

        if(string.length() == 0) {
            string.append("ПУСТО");
            log.info("<---- выход из nameList() ---->");
            return string.toString();
        }
        log.info("<---- выход из nameList() ---->");
        return string.toString();
    }

    @Override
    public String nameAndDosageList(List<Medicine> meds) {
        log.info("----> вход в nameAndDosageList() <----");
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
            log.info("<---- выход из nameAndDosageList() ---->");
            return string.toString();
        }
        log.info("<---- выход из nameAndDosageList() ---->");
        return string.toString();
    }

    @Override
    public String allInfoList(List<Medicine> meds) {
        log.info("----> вход в allInfoList() <----");
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
                    .append(med.getTextExpDate())
                    .append("\n");
        }

        if(string.length() == 0) {
            log.info("<---- выход из allInfoList() ---->");
            return string.append("ПУСТО").toString();
        }
        log.info("<---- выход из allInfoList() ---->");
        return string.toString();
    }
}
