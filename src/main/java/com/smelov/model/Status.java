package com.smelov.model;

import com.smelov.entity.Medicine;

public enum Status {
    NONE,
    NAME, DOSAGE, DOSAGE_TYPE, QUANTITY, QUANTITY_TYPE, EXP_DATE,
    DEL, EDIT, EDIT_NAME, EDIT_DOSAGE, EDIT_EXP, EDIT_QTY;

    private Medicine medicine;

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
