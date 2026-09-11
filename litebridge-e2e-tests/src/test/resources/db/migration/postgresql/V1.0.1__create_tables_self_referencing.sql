CREATE TABLE lb.self_referencing
(
    id        NUMERIC(10) NOT NULL PRIMARY KEY,
    my_var    VARCHAR(255),
    parent_id NUMERIC(10) REFERENCES lb.self_referencing (id)
);