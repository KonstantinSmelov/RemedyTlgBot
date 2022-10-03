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
    public String nameAndDosageListSortedByLastAction(List<Medicine> meds) {
        log.info("----> вход в nameAndDosageListSortedByLastAction() <----");
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
            log.info("<---- выход из nameAndDosageListSortedByLastAction() ---->");
            return string.toString();
        }
        log.info("<---- выход из nameAndDosageListSortedByLastAction() ---->");
        return string.toString();
    }

    @Override
    public String allInfoListSortedByName(List<Medicine> meds) {
        log.info("----> вход в allInfoListSortedByName() <----");
        StringBuilder string = new StringBuilder();
        meds.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

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
            log.info("<---- выход из allInfoListSortedByName() ---->");
            return string.append("ПУСТО").toString();
        }
        log.info("<---- выход из allInfoListSortedByName() ---->");
        return string.toString();
    }

    @Override
    public String allInfoListSortedByLastAction(List<Medicine> meds) {
        log.info("----> вход в allInfoListSortedByLastAction() <----");
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
            log.info("<---- выход из allInfoListSortedByLastAction() ---->");
            return string.append("ПУСТО").toString();
        }
        log.info("<---- выход из allInfoListSortedByLastAction() ---->");
        return string.toString();
    }

    @Override
    public String allInfoListSortedByExpDate(List<Medicine> meds) {
        log.info("----> вход в allInfoListSortedByExpDate() <----");
        StringBuilder string = new StringBuilder();
        meds.sort((o1, o2) -> o1.getTextExpDate().compareTo(o2.getTextExpDate()));

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
            log.info("<---- выход из allInfoListSortedByExpDate() ---->");
            return string.append("ПУСТО").toString();
        }
        log.info("<---- выход из allInfoListSortedByExpDate() ---->");
        return string.toString();
    }
}
