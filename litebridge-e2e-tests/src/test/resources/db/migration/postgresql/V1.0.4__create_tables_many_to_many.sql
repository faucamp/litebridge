CREATE TABLE lb."group"
(
    group_name VARCHAR(50)  NOT NULL PRIMARY KEY,
    group_desc VARCHAR(255) NOT NULL
);

CREATE TABLE lb.person_group
(
    person_id  NUMERIC(10)   NOT NULL REFERENCES lb.person (person_id),
    group_name VARCHAR(50) NOT NULL REFERENCES lb."group" (group_name),

    PRIMARY KEY (person_id, group_name)
);