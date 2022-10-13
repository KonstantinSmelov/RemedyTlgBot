package com.smelov.service;

import com.smelov.entity.Medicine;

import java.util.Comparator;
import java.util.List;

public interface TextMessageService {
    String nameList(List<Medicine> meds);
    String allInfoList(List<Medicine> meds);
    String nameAndDosageList(List<Medicine> meds);
}
