package com.smelov.service;

import com.smelov.entity.Medicine;

import java.util.List;

public interface TextMessageService {
    String nameAndDosageListSortedByLastAction(List<Medicine> meds);
    String allInfoListSortedByName(List<Medicine> meds);
    String allInfoListSortedByLastAction(List<Medicine> meds);
    String allInfoListSortedByExpDate(List<Medicine> meds);
}
