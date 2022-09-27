package com.smelov.model;

import com.smelov.entity.Medicine;

public enum Status {
    NONE,
    NAME, DOSAGE, DOSAGE_TYPE, QUANTITY, QUANTITY_TYPE, EXP_DATE,
    DEL, EDIT;

    private Medicine medicine;
    private EditStatus editStatus = EditStatus.NONE;

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

    @Override
    public String toString() {
        return super.toString() + " (привязан к "  + medicine.getName() + ")";
    }
}
