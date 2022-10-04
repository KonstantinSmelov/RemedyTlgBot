package com.smelov.model;

import com.smelov.entity.Medicine;

import java.util.Comparator;

public enum Status {
    NONE, ADD, DEL, EDIT;

    private Medicine medicine;
    private EditStatus editStatus = EditStatus.NONE;
    private AddStatus addStatus = AddStatus.NONE;
    private Comparator<Medicine> comparator;

    public AddStatus getAddStatus() {
        return addStatus;
    }

    public Status setAddStatus(AddStatus addStatus) {
        this.addStatus = addStatus;
        return this;
    }

    public EditStatus getEditStatus() {
        return editStatus;
    }

    public Status setEditStatus(EditStatus editStatus) {
        this.editStatus = editStatus;
        return this;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public Status setMedicine(Medicine medicine) {
        this.medicine = medicine;
        return this;
    }

    public Comparator<Medicine> getComparator() {
        return comparator;
    }

    public Status setComparator(Comparator<Medicine> comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public String toString() {
//        return super.toString() + "; AddStatus: " + getAddStatus() + "; EditStatus: " + getEditStatus() + " (привязан к "  + medicine.getName() + ")";
        return this.name() + "; AddStatus: " + getAddStatus() + "; EditStatus: " + getEditStatus() + " (привязан к "  + medicine.getName() + ")";
    }
}
