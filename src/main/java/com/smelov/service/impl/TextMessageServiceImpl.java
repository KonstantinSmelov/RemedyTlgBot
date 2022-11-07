package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.service.TextMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;


@Service
@Slf4j
public class TextMessageServiceImpl implements TextMessageService {

    @Override
    public String nameList(List<Medicine> meds) {
        log.debug("----> вход в nameList()");
        StringBuilder string = new StringBuilder();
        string.append("<b>Препараты в наличии:</b> \n\n");
        int x = 1;

        for (Medicine med : meds) {
            string
                    .append("<b>")
                    .append(x++)
                    .append("</b>")
                    .append(" - ");

            if (0 < LocalDate.now().compareTo(med.getExpDate().toLocalDate())) {
                string
                        .append("<del>")
                        .append(med.getName())
                        .append("</del>");
            } else if (0 < LocalDate.now().minus(1, ChronoUnit.MONTHS).compareTo(med.getExpDate().toLocalDate())) {
                string
                        .append("<del>")
                        .append(med.getName())
                        .append("</del>");
            } else {
                string.append(med.getName());
            }

            string.append("\n");
        }

        if (string.length() == 0) {
            string.append("ПУСТО");
        }

        log.debug("<---- выход из nameList(): {}", string);
        return string.toString();
    }

    @Override
    public String nameAndDosageList(List<Medicine> meds) {
        log.debug("----> вход в nameAndDosageList()");
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

        if (string.length() == 0) {
            string.append("ПУСТО");
        }

        log.debug("<---- выход из nameAndDosageList(): {}", string);
        return string.toString();
    }

    @Override
    public String allInfoList(List<Medicine> meds) {
        log.debug("----> вход в allInfoList()");
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

        if (string.length() == 0) {
            string.append("ПУСТО");
        }

        log.debug("<---- выход из allInfoList(): {}", string);
        return string.toString();
    }
}
