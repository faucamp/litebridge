CREATE TABLE lb.tenant_a
(
    id         NUMERIC(10) NOT NULL PRIMARY KEY,
    name       VARCHAR(255),
    setting_id NUMERIC(10)
);

CREATE TABLE lb.tenant_setting_a
(
    id            NUMERIC(10) NOT NULL PRIMARY KEY,
    setting_key   VARCHAR(255),
    setting_value VARCHAR(255)
);

CREATE TABLE lb.account_a
(
    id         NUMERIC(10) NOT NULL PRIMARY KEY,
    TENANT_id  NUMERIC(10),
    name       VARCHAR(255),
    setting_id NUMERIC(10)
);

CREATE TABLE lb.account_setting_a
(
    id            NUMERIC(10) NOT NULL PRIMARY KEY,
    setting_key   VARCHAR(255),
    setting_value VARCHAR(255)
);
