package com.smelov.bot.entity.idclass;

import lombok.*;

import java.io.Serializable;
import java.sql.Date;


@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineId implements Serializable {

    private String name;
    private String dosage;
    private Date expDate;
}
