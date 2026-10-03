CREATE SEQUENCE lb.composite_pk1_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE lb.composite_pk2_seq START WITH 2 INCREMENT BY 1;

CREATE TABLE lb.comp_pk_lookup
(
    lookup_id   NUMERIC(10) NOT NULL PRIMARY KEY,
    lookup_name VARCHAR(50) NOT NULL
);

CREATE TABLE lb.comp_pk_fk_test
(
    lookup_id NUMERIC(10) NOT NULL REFERENCES lb.comp_pk_lookup (lookup_id),
    test_id   NUMERIC(10) NOT NULL,
    test_desc VARCHAR(50),

    PRIMARY KEY (lookup_id, test_id)
);

CREATE TABLE lb.comp_pk_simple
(
    pk1       NUMERIC(10) NOT NULL,
    pk2       NUMERIC(10) NOT NULL,
    test_desc VARCHAR(50),

    PRIMARY KEY (pk1, pk2)
);