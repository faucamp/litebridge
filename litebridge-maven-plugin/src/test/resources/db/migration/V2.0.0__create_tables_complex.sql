create table LB.OTP_TYPE
(
    OTP_TYPE_ID   NUMBER(15)    not null
        constraint OTP_TYPE_PK
            primary key,
    OTP_TYPE_DESC VARCHAR2(100) not null
);

create table LB.OTP_VALIDATION
(
    OTP_VAL_ID             NUMBER(15)   not null
        constraint OTP_VALIDATION_PK
            primary key,
    OTP_TYPE               NUMBER(15)   not null,
    RECEIVING_ENTITY_NO    NUMBER(15)   not null,
    DISCLOSING_ENTITY_NO   NUMBER(15)   not null,
    OTP_VAL                VARCHAR2(15) not null,
    NOTIFICATION_REFERENCE NUMBER(15)   not null,
    OTP_VALIDATED          NUMBER(1)    not null,
    RETRY_ATTEMPTS         NUMBER(3)    not null,
    EFF_FROM               DATE         not null,
    EFF_TO                 DATE         not null
);

create index LB.OTP_VALIDATION_002N
    on LB.OTP_VALIDATION (OTP_TYPE);

create index LB.OTP_VALIDATION_003N
    on LB.OTP_VALIDATION (RECEIVING_ENTITY_NO, DISCLOSING_ENTITY_NO);

create table LB.OS_CONFIG_TYPE
(
    CONFIG_TYPE_ID VARCHAR2(100) not null
        constraint OS_CONFIG_TYPE_PK
            primary key,
    CONFIG_DESC    VARCHAR2(255) not null,
    CONFIG_DEFAULT VARCHAR2(255)
);

create table LB.OS_CONFIG
(
    CONFIG_TYPE_ID VARCHAR2(100)  not null
        constraint OS_CONFIG_PK
            primary key
        constraint OS_CONFIG_FK1
            references LB.OS_CONFIG_TYPE,
    CONFIG_VAL     VARCHAR2(4000) not null
);

create table LB.OS_GROUP
(
    GROUP_ID         VARCHAR2(100)       not null
        constraint OS_GROUP_PK
            primary key,
    GROUP_DESC       VARCHAR2(255),
    HOST_REGEX       VARCHAR2(255),
    DEFAULT_DISABLED NUMBER(1) default 0 not null
);

create table LB.OS_AUTH_TYPE
(
    AUTH_TYPE_ID   VARCHAR2(50)  not null
        constraint OS_AUTH_TYPE_PK
            primary key,
    AUTH_TYPE_DESC VARCHAR2(255) not null
);

create table LB.OS_AUTH
(
    AUTH_ID       VARCHAR2(50)  not null
        constraint OS_AUTH_PK
            primary key,
    AUTH_TYPE_ID  VARCHAR2(50)  not null
        constraint OS_AUTH_FK1
            references LB.OS_AUTH_TYPE,
    AUTH_URL      VARCHAR2(255) not null,
    CLIENT_ID     VARCHAR2(100),
    CLIENT_SECRET VARCHAR2(100),
    USERNAME      VARCHAR2(100),
    PASSWORD      VARCHAR2(100)
);

create index LB.OS_AUTH_002N
    on LB.OS_AUTH (AUTH_TYPE_ID);

create table LB.OS_AUTH_GROUP
(
    GROUP_ID VARCHAR2(100) not null
        constraint OS_AUTH_GROUP_FK1
            references LB.OS_GROUP,
    AUTH_ID  VARCHAR2(50)  not null
        constraint OS_AUTH_GROUP_FK2
            references LB.OS_AUTH,
    constraint OS_AUTH_GROUP_PK
        primary key (GROUP_ID, AUTH_ID)
);

create table LB.OS_SRV_CHECK_TP
(
    SRV_CHECK_TP_ID VARCHAR2(10)  not null
        constraint OS_SRV_CHECK_TP_PK
            primary key,
    CHECK_DESC      VARCHAR2(200) not null
);

create table LB.OS_FEAT_APP
(
    FEAT_APP_ID   VARCHAR2(100) not null
        constraint OS_FEAT_APP_PK
            primary key,
    FEAT_APP_DESC VARCHAR2(255) not null
);

create table LB.OS_FEAT_CAT
(
    FEAT_APP_ID   VARCHAR2(100) not null
        constraint OS_FEAT_CAT_FK1
            references LB.OS_FEAT_APP,
    FEAT_CAT_ID   VARCHAR2(100) not null,
    FEAT_CAT_DESC VARCHAR2(255),
    constraint OS_FEAT_CAT_PK
        primary key (FEAT_APP_ID, FEAT_CAT_ID)
);

create table LB.OS_FEATURE
(
    FEATURE_ID   VARCHAR2(100) not null
        constraint OS_FEATURE_PK
            primary key,
    FEAT_APP_ID  VARCHAR2(100) not null,
    FEAT_CAT_ID  VARCHAR2(100) not null,
    FEATURE_DESC VARCHAR2(500)
);

create index LB.OS_FEATURE_002N
    on LB.OS_FEATURE (FEAT_APP_ID, FEAT_CAT_ID);

create table LB.OS_STATUS
(
    STATUS_ID   NUMBER(10)    not null
        constraint OS_STATUS_PK
            primary key,
    STATUS_VAL  VARCHAR2(50)  not null,
    STATUS_DESC VARCHAR2(255) not null
);

create table LB.OS_HIST_HEALTHCHECK
(
    HIST_HEALTHCHECK_ID NUMBER(15) not null
        constraint OS_HIST_HEALTHCHECK_PK
            primary key,
    EXECUTED_AT         DATE       not null,
    STATUS              NUMBER(10) not null
        constraint OS_HIST_HEALTHCHECK_FK1
            references LB.OS_STATUS,
    STATUS_MSG          VARCHAR2(100)
);

create index LB.OS_HIST_HEALTHCHECK_002N
    on LB.OS_HIST_HEALTHCHECK (EXECUTED_AT);

create index LB.OS_HIST_HEALTHCHECK_003N
    on LB.OS_HIST_HEALTHCHECK (STATUS);

create table LB.OS_HIST_STAT_DET
(
    HIST_STAT_DET_ID NUMBER(15) not null
        constraint OS_HIST_STAT_DET_PK
            primary key,
    STATUS_DETAIL    BLOB
);

create table LB.OS_HIST_SERVICE
(
    HIST_SERVICE_ID     NUMBER(15)          not null
        constraint OS_HIST_SERVICE_PK
            primary key,
    HIST_HEALTHCHECK_ID NUMBER(15)          not null
        constraint OS_HIST_SERVICE_FK1
            references LB.OS_HIST_HEALTHCHECK,
    SERVICE_ID          VARCHAR2(100)       not null,
    STATUS              NUMBER(10)          not null
        constraint OS_HIST_SERVICE_FK2
            references LB.OS_STATUS,
    STATUS_MSG          VARCHAR2(100),
    STATUS_PROPAGATED   NUMBER(1) default 0 not null,
    HIST_STAT_DET_ID    NUMBER(15)
);

create index LB.OS_HIST_SERVICE_002N
    on LB.OS_HIST_SERVICE (HIST_HEALTHCHECK_ID);

create index LB.OS_HIST_SERVICE_003N
    on LB.OS_HIST_SERVICE (STATUS);

create index LB.OS_HIST_SERVICE_004N
    on LB.OS_HIST_SERVICE (HIST_STAT_DET_ID);

create table LB.OS_HIST_SRV_DEP
(
    HIST_SRV_DEP_ID       NUMBER(15)    not null
        constraint OS_HIST_SRV_DEP_PK
            primary key,
    HIST_SERVICE_ID       NUMBER(15)    not null
        constraint OS_HIST_SRV_DEP_FK1
            references LB.OS_HIST_SERVICE,
    CLIENT_SERV_COMPNT_ID VARCHAR2(100),
    DEP_SERVICE_ID        VARCHAR2(100) not null,
    DEP_SERV_COMPNT_ID    VARCHAR2(100),
    DEP_DESC              VARCHAR2(500)
);

create index LB.OS_HIST_SRV_DEP_002N
    on LB.OS_HIST_SRV_DEP (HIST_SERVICE_ID);

create table LB.OS_HIST_SRV_CNTB
(
    HIST_SRV_CNTB_ID NUMBER(15)    not null
        constraint OS_HIST_SRV_CNTB_PK
            primary key,
    HIST_SERVICE_ID  NUMBER(15)    not null
        constraint OS_HIST_SRV_CNTB_FK1
            references LB.OS_HIST_SERVICE,
    DEP_SERVICE_ID   VARCHAR2(100) not null
);

create index LB.OS_HIST_SRV_CNTB_002N
    on LB.OS_HIST_SRV_CNTB (HIST_SERVICE_ID);

create table LB.OS_HIST_SRV_INST
(
    HIST_SRV_INST_ID NUMBER(15)    not null
        constraint OS_HIST_SRV_INST_PK
            primary key,
    HIST_SERVICE_ID  NUMBER(15)    not null
        constraint OS_HIST_SRV_INST_FK1
            references LB.OS_HIST_SERVICE,
    HOST             VARCHAR2(255) not null,
    PORT             NUMBER(10)    not null,
    STATUS           NUMBER(10)    not null
        constraint OS_HIST_SRV_INST_FK2
            references LB.OS_STATUS,
    STATUS_MSG       VARCHAR2(100),
    HIST_STAT_DET_ID NUMBER(15)
);

create index LB.OS_HIST_SRV_INST_002N
    on LB.OS_HIST_SRV_INST (HIST_SERVICE_ID);

create index LB.OS_HIST_SRV_INST_003N
    on LB.OS_HIST_SRV_INST (STATUS);

create index LB.OS_HIST_SRV_INST_004N
    on LB.OS_HIST_SRV_INST (HIST_STAT_DET_ID);

create table LB.OS_HIST_SRV_CMPT
(
    HIST_SRV_CMPT_ID NUMBER(15)    not null
        constraint OS_HIST_SRV_CMPT_PK
            primary key,
    HIST_SERVICE_ID  NUMBER(15)    not null
        constraint OS_HIST_SRV_CMPT_FK1
            references LB.OS_HIST_SERVICE,
    SERV_COMPNT_ID   VARCHAR2(100) not null,
    STATUS           NUMBER(10)    not null
);

create index LB.OS_HIST_SRV_CMPT_002N
    on LB.OS_HIST_SRV_CMPT (HIST_SERVICE_ID);

create index LB.OS_HIST_SRV_CMPT_003N
    on LB.OS_HIST_SRV_CMPT (STATUS);

create table LB.OS_HIST_FEATURE
(
    HIST_FEATURE_ID     NUMBER(15)    not null
        constraint OS_HIST_FEATURE_PK
            primary key,
    HIST_HEALTHCHECK_ID NUMBER(15)    not null
        constraint OS_HIST_FEATURE_FK1
            references LB.OS_HIST_HEALTHCHECK,
    FEATURE_ID          VARCHAR2(100) not null,
    STATUS              NUMBER(10)    not null
        constraint OS_HIST_FEATURE_FK2
            references LB.OS_STATUS,
    STATUS_MSG          VARCHAR2(100)
);

create index LB.OS_HIST_FEATURE_002N
    on LB.OS_HIST_FEATURE (HIST_HEALTHCHECK_ID);

create index LB.OS_HIST_FEATURE_003N
    on LB.OS_HIST_FEATURE (STATUS);

create table LB.OS_MAINTENANCE
(
    MAINTENANCE_ID NUMBER(15)   not null
        constraint OS_MAINTENANCE_PK
            primary key,
    START_CRON     VARCHAR2(50) not null,
    DURATION_MINS  NUMBER(10)   not null
);

create table LB.OS_MNT_GROUP
(
    MAINTENANCE_ID NUMBER(15)    not null
        constraint OS_MNT_GROUP_FK1
            references LB.OS_MAINTENANCE,
    GROUP_ID       VARCHAR2(100) not null
        constraint OS_MNT_GROUP_FK2
            references LB.OS_GROUP,
    constraint OS_MNT_GROUP_PK
        primary key (MAINTENANCE_ID, GROUP_ID)
);

create table LB.OS_TEST_REPORT
(
    TEST_REPORT_ID NUMBER(15)   not null
        constraint OS_TEST_REPORT_PK
            primary key,
    ENV            VARCHAR2(10) not null,
    EXECUTED_AT    DATE         not null,
    FEATURES       VARCHAR2(2000),
    ZIPPED_REPORT  BLOB
);

create index LB.OS_TEST_REPORT_002N
    on LB.OS_TEST_REPORT (ENV);

create index LB.OS_TEST_REPORT_003N
    on LB.OS_TEST_REPORT (EXECUTED_AT);

create table LB.OS_SERVICE_TYPE
(
    SERVICE_TYPE_ID   VARCHAR2(100) not null
        constraint OS_SERVICE_TYPE_PK
            primary key,
    SERVICE_TYPE_DESC VARCHAR2(255),
    SERVICE_ID_REGEX  VARCHAR2(255)
);

create table LB.OS_SERVICE
(
    SERVICE_ID       VARCHAR2(100)           not null
        constraint OS_SERVICE_PK
            primary key,
    SERVICE_DESC     VARCHAR2(255),
    GROUP_ID         VARCHAR2(100)
        constraint OS_SERVICE_FK1
            references LB.OS_GROUP,
    STATIC_ONLY      NUMBER(1)  default 0    not null,
    ACTUATOR_INSPECT NUMBER(1)  default 1    not null,
    CONTEXT_ROOT     VARCHAR2(255),
    DEF_RESP_SLA_MS  NUMBER(10) default 1000 not null,
    MIN_INSTANCES    NUMBER(10) default 0    not null,
    DISABLED         NUMBER(1)  default 0    not null,
    SERVICE_TYPE_ID  VARCHAR2(100)
        constraint OS_SERVICE_FK2
            references LB.OS_SERVICE_TYPE
);

create index LB.OS_SERVICE_002N
    on LB.OS_SERVICE (GROUP_ID);

create table LB.OS_SERVICE_INSTANCE
(
    SERVICE_ID   VARCHAR2(100) not null
        constraint OS_SERVICE_INSTANCE_FK1
            references LB.OS_SERVICE,
    HOST         VARCHAR2(255) not null,
    PORT         NUMBER(10)    not null,
    CONTEXT_ROOT VARCHAR2(255),
    constraint OS_SERVICE_INSTANCE_PK
        primary key (SERVICE_ID, HOST, PORT)
);

create table LB.OS_SERV_COMPNT
(
    SERV_COMPNT_ID     VARCHAR2(100)       not null
        constraint OS_SERV_COMPNT_PK
            primary key,
    SERVICE_ID         VARCHAR2(100)       not null
        constraint OS_SERV_COMPNT_FK1
            references LB.OS_SERVICE,
    COMPNT_DESC        VARCHAR2(255)       not null,
    ACTUATOR_COMPONENT VARCHAR2(100),
    INDEPENDENT        NUMBER(1) default 1 not null
);

create index LB.OS_SERV_COMPNT_002N
    on LB.OS_SERV_COMPNT (SERVICE_ID);

create table LB.OS_AUTH_SERVICE
(
    SERVICE_ID VARCHAR2(100) not null
        constraint OS_AUTH_SERVICE_FK1
            references LB.OS_SERVICE,
    AUTH_ID    VARCHAR2(50)  not null
        constraint OS_AUTH_SERVICE_FK2
            references LB.OS_AUTH,
    constraint OS_AUTH_SERVICE_PK
        primary key (SERVICE_ID, AUTH_ID)
);

create table LB.OS_SERVICE_CHECK
(
    SERVICE_CHECK_ID VARCHAR2(100) not null
        constraint OS_SERVICE_CHECK_PK
            primary key,
    SERVICE_ID       VARCHAR2(100) not null
        constraint OS_SERVICE_CHECK_FK1
            references LB.OS_SERVICE,
    SERV_COMPNT_ID   VARCHAR2(100)
        constraint OS_SERVICE_CHECK_FK2
            references LB.OS_SERV_COMPNT,
    CHECK_DESC       VARCHAR2(200) not null,
    SRV_CHECK_TP_ID  VARCHAR2(10)  not null
        constraint OS_SERVICE_CHECK_FK3
            references LB.OS_SRV_CHECK_TP,
    REQ_METHOD       VARCHAR2(50),
    REQ_PATH         VARCHAR2(500),
    REQ_PAYLOAD      VARCHAR2(4000),
    REQ_CNTNT_TYPE   VARCHAR2(100),
    RESP_CODE        NUMBER(5),
    RESP_CONTAINS    VARCHAR2(4000),
    RESP_SLA_MS      NUMBER(10)
);

create index LB.OS_SERVICE_CHECK_002N
    on LB.OS_SERVICE_CHECK (SERVICE_ID);

create index LB.OS_SERVICE_CHECK_003N
    on LB.OS_SERVICE_CHECK (SERV_COMPNT_ID);

create index LB.OS_SERVICE_CHECK_004N
    on LB.OS_SERVICE_CHECK (SRV_CHECK_TP_ID);

create table LB.OS_CLIENT
(
    CLIENT_CLASS   VARCHAR2(255) not null
        constraint OS_CLIENT_PK
            primary key,
    CLIENT_DESC    VARCHAR2(255),
    SERVICE_ID     VARCHAR2(100) not null
        constraint OS_CLIENT_FK1
            references LB.OS_SERVICE,
    SERV_COMPNT_ID VARCHAR2(100)
        constraint OS_CLIENT_FK2
            references LB.OS_SERV_COMPNT
);

create index LB.OS_CLIENT_002N
    on LB.OS_CLIENT (SERVICE_ID);

create index LB.OS_CLIENT_003N
    on LB.OS_CLIENT (SERV_COMPNT_ID);

create table LB.OS_SERVICE_DEP
(
    SERVICE_DEP_ID        VARCHAR2(100) not null
        constraint OS_SERVICE_DEP_PK
            primary key,
    CLIENT_SERVICE_ID     VARCHAR2(100) not null
        constraint OS_SERVICE_DEP_FK1
            references LB.OS_SERVICE,
    CLIENT_SERV_COMPNT_ID VARCHAR2(100)
        constraint OS_SERVICE_DEP_FK2
            references LB.OS_SERV_COMPNT,
    DEP_SERVICE_ID        VARCHAR2(100) not null
        constraint OS_SERVICE_DEP_FK3
            references LB.OS_SERVICE,
    DEP_SERV_COMPNT_ID    VARCHAR2(100)
        constraint OS_SERVICE_DEP_FK4
            references LB.OS_SERV_COMPNT,
    DEP_DESC              VARCHAR2(500)
);

create index LB.OS_SERVICE_DEP_002N
    on LB.OS_SERVICE_DEP (CLIENT_SERVICE_ID);

create index LB.OS_SERVICE_DEP_003N
    on LB.OS_SERVICE_DEP (CLIENT_SERV_COMPNT_ID);

create index LB.OS_SERVICE_DEP_004N
    on LB.OS_SERVICE_DEP (DEP_SERVICE_ID);

create index LB.OS_SERVICE_DEP_005N
    on LB.OS_SERVICE_DEP (DEP_SERV_COMPNT_ID);

create table LB.OS_FEAT_SERVICE
(
    FEAT_SERVICE_ID VARCHAR2(100) not null
        constraint OS_FEAT_SERVICE_PK
            primary key,
    FEATURE_ID      VARCHAR2(100) not null
        constraint OS_FEAT_SERVICE_FK1
            references LB.OS_FEATURE,
    SERVICE_ID      VARCHAR2(100) not null
        constraint OS_FEAT_SERVICE_FK2
            references LB.OS_SERVICE,
    SERV_COMPNT_ID  VARCHAR2(100)
        constraint OS_FEAT_SERVICE_FK3
            references LB.OS_SERV_COMPNT
);

create index LB.OS_FEAT_SERVICE_002N
    on LB.OS_FEAT_SERVICE (FEATURE_ID);

create index LB.OS_FEAT_SERVICE_003N
    on LB.OS_FEAT_SERVICE (SERVICE_ID);

create index LB.OS_FEAT_SERVICE_004N
    on LB.OS_FEAT_SERVICE (SERV_COMPNT_ID);

create table LB.OS_MNT_SERVICE
(
    MAINTENANCE_ID NUMBER(15)    not null
        constraint OS_MNT_SERVICE_FK1
            references LB.OS_MAINTENANCE,
    SERVICE_ID     VARCHAR2(100) not null
        constraint OS_MNT_SERVICE_FK2
            references LB.OS_SERVICE,
    constraint OS_MNT_SERVICE_PK
        primary key (MAINTENANCE_ID, SERVICE_ID)
);

