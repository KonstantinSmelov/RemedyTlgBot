package com.smelov.entity;

import com.smelov.entity.idclass.MedicineId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "medicines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MedicineId.class)
public class Medicine {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "dosage")
    private String dosage;

    @Id
    @Column(name = "exp_date")
    private Date expDate;

    @Column(name = "quantity")
    private String quantity;

    public Medicine(Medicine medicine) {
        this.setName(medicine.getName());
        this.setDosage(medicine.getDosage());
        this.setExpDate(medicine.getExpDate());
        this.setQuantity(medicine.getQuantity());
    }

    public String getTextExpDate() {
//        return this.getExpDate().toString().substring(0, getExpDate().toString().length() - 3);
        return this.getExpDate().toLocalDate().getYear() + "-" + this.getExpDate().toLocalDate().getMonth().getValue();
    }
}
