CREATE TABLE lb.blob_test
(
    id NUMERIC(10) NOT NULL PRIMARY KEY,
    blob_data BYTEA
);

CREATE TABLE lb.clob_test
(
    id NUMERIC(10) NOT NULL PRIMARY KEY,
    clob_data TEXT
)