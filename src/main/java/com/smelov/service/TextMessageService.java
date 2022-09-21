package com.smelov.service;

import com.smelov.bot.entity.Medicine;

import java.util.List;

public interface TextMessageService {
    String medNameAndDosageToText(List<Medicine> meds);
    String allMedInfoToText(List<Medicine> meds);
}
