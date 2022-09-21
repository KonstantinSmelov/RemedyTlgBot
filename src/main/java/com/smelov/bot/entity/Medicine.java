package com.smelov.bot.entity;

import com.smelov.bot.entity.idclass.MedicineId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;

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

}
