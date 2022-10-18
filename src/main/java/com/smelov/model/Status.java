package com.smelov.model;

import com.smelov.entity.Medicine;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data
@Builder
public class Status {

    private MainStatus mainStatus = MainStatus.NONE;
    private EditStatus editStatus = EditStatus.NONE;
    private AddStatus addStatus = AddStatus.NONE;
    private Comparator<Medicine> comparator;
    private Medicine medicine = new Medicine();

    @Override
    public String toString() {
        return "MainStatus: " + getMainStatus() +
                "; AddStatus: " + getAddStatus() +
                "; EditStatus: " + getEditStatus() +
                "; Medicine: " +
                ((medicine != null) ? medicine.getName() : "---");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return mainStatus == status.mainStatus && editStatus == status.editStatus && addStatus == status.addStatus && Objects.equals(medicine, status.medicine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainStatus, editStatus, addStatus, medicine);
    }
}
