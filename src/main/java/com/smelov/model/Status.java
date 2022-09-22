package com.smelov.model;

import com.smelov.entity.Medicine;

public enum Status {
    NAME, DOSAGE, DOSAGE_TYPE, QUANTITY, QUANTITY_TYPE, EXP_DATE, DEL;

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
