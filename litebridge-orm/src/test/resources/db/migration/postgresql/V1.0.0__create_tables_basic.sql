CREATE SEQUENCE lb.person_seq START WITH 1 MAXVALUE 999 INCREMENT BY 1;
CREATE SEQUENCE lb.account_seq START WITH 1 MAXVALUE 999 INCREMENT BY 1;

CREATE TABLE lb.person
(
    person_id  NUMERIC(10) NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    surname    VARCHAR(255),
    age        NUMERIC(3),
    eye_colour VARCHAR(255)
);

CREATE TABLE lb.account
(
    account_id   NUMERIC(10) NOT NULL PRIMARY KEY,
    account_name VARCHAR(255) NOT NULL,
    balance      NUMERIC(10) NOT NULL,
    person_id    NUMERIC(10) NOT NULL REFERENCES lb.person (person_id)
);
