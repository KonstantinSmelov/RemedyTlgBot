package com.smelov.model;

import com.smelov.entity.Medicine;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class Status {

    private MainStatus mainStatus;
    private EditStatus editStatus;
    private AddStatus addStatus;
    private Comparator<Medicine> comparator;
    private Medicine medicine;
    private Set<Integer> userMessageIds;

    @Override
    public String toString() {
        return "MainStatus: " + getMainStatus() +
                "; AddStatus: " + getAddStatus() +
                "; EditStatus: " + getEditStatus() +
                "; Medicine: " + ((medicine != null) ? medicine.getName() : "---") +
                "; MsgIds: " + userMessageIds;
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
