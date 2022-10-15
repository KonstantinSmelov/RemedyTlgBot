package com.smelov.model;

import com.smelov.entity.Medicine;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Data
@Builder
public class Status {

    private MainStatus mainStatus = MainStatus.NONE;
    private EditStatus editStatus = EditStatus.NONE;
    private AddStatus addStatus = AddStatus.NONE;
    private Comparator<Medicine> comparator;
    private Medicine medicine;

    @Override
    public String toString() {
//        return super.toString() + "; AddStatus: " + getAddStatus() + "; EditStatus: " + getEditStatus() + " (привязан к "  + medicine.getName() + ")";
        return "MainStatus: " + getMainStatus() +
                "; AddStatus: " + getAddStatus() +
                "; EditStatus: " + getEditStatus() +
                "; Medicine: " +
                ((medicine != null) ? medicine.getName() : "---");
//                " (привязан к "  + ((medicine != null) ? medicine.getName() : "null")
//                + ")";
    }
}
