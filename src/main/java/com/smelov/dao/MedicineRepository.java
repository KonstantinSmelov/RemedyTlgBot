package com.smelov.dao;

import com.smelov.entity.Medicine;
import com.smelov.entity.idclass.MedicineId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, MedicineId> {

    List<Medicine> findMedicineByName(String name);
    Medicine getByNameAndDosageAndExpDate(String name, String dosage, Date date);
    void deleteByNameAndDosageAndExpDate(String name, String dosage, Date date);
}
