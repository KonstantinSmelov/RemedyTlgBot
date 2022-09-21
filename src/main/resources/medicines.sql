create table medicines
(
    name varchar,
    dosage varchar,
    quantity varchar,
    exp_date DATE,
    primary key(name, dosage, exp_date)
)