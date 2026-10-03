-- Tables for test where a DTO is shared between two other DTOs,
-- and the shared DTO is persisted to different tables as a result
CREATE SEQUENCE lb.application_seq START WITH 1 MAXVALUE 999 INCREMENT BY 1;
CREATE SEQUENCE lb.server_seq START WITH 1 MAXVALUE 999 INCREMENT BY 1;

-- Status associated with an application instance
CREATE TABLE lb.application_status
(
    code    NUMERIC(10) NOT NULL PRIMARY KEY,
    message VARCHAR(255)
);

-- Application instance
CREATE TABLE lb.application
(
    name        VARCHAR(255) NOT NULL PRIMARY KEY,
    status_code NUMERIC(10) REFERENCES lb.application_status (code)
);

-- Status associated with a server instance
CREATE TABLE lb.server_status
(
    status_code NUMERIC(10) NOT NULL PRIMARY KEY,
    message     VARCHAR(255)
);

-- Server instance
CREATE TABLE lb.server
(
    host               VARCHAR(255) NOT NULL PRIMARY KEY,
    server_status_code NUMERIC(10) REFERENCES lb.server_status (status_code)
);
