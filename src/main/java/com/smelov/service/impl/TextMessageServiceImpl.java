package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.service.TextMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TextMessageServiceImpl implements TextMessageService {

    @Override
    public String medNameAndDosageToText(List<Medicine> meds) {
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
            return string.toString();
        }

        return string.toString();
    }

    @Override
    public String allMedInfoToText(List<Medicine> meds) {
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
            return string.append("ПУСТО").toString();
        }

        return string.toString();
    }
}
