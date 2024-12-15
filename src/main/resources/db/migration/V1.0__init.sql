-- Добавляем расширение для работы с географическими объектами
CREATE EXTENSION if not exists postgis;

-- Создание типа 'FILTER_TYPE', если он не существует
DO
$$
    BEGIN
        PERFORM 1
        FROM pg_type
        WHERE typname = 'FILTER_TYPE';
        IF NOT FOUND THEN
            CREATE TYPE matcher.FILTER_TYPE AS ENUM ('ALL_MATCH', 'ANY_MATCH', 'NO_FILTER');
        END IF;
    END
$$;

-- Автоматический каст строки в тип FILTER_TYPE
DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_cast
            WHERE castsource::regtype = 'character varying'::regtype
              AND casttarget::regtype = 'matcher.FILTER_TYPE'::regtype
        ) THEN
            EXECUTE 'CREATE CAST (character varying as matcher.FILTER_TYPE) WITH INOUT AS IMPLICIT';
        END IF;
    END
$$;

-- Создание типа 'APPOINTMENT_STATUS', если он не существует
DO
$$
    BEGIN
        PERFORM 1
        FROM pg_type
        WHERE typname = 'APPOINTMENT_STATUS';
        IF NOT FOUND THEN
            CREATE TYPE matcher.APPOINTMENT_STATUS AS ENUM ('REQUESTED', 'APPOINTED', 'DONE', 'CANCELED');
        END IF;
    END
$$;

-- Автоматический каст строки в тип APPOINTMENT_STATUS
DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_cast
            WHERE castsource::regtype = 'character varying'::regtype
              AND casttarget::regtype = 'matcher.APPOINTMENT_STATUS'::regtype
        ) THEN
            EXECUTE 'CREATE CAST (character varying as matcher.APPOINTMENT_STATUS) WITH INOUT AS IMPLICIT';
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS LOCATION (
    ID uuid PRIMARY KEY,
    REGION varchar,
    CITY varchar,
    COUNTRY varchar,
    STREET varchar,
    HOUSE varchar,
    POSITION geometry(POINT, 4326) NOT NULL
);

CREATE TABLE IF NOT EXISTS PERSON(
    ID uuid PRIMARY KEY,
    GENDER varchar(1),
    AGE int,
    LOCATION_ID uuid references LOCATION,
    SEARCH_AREA_METERS int default 5000,
    GROUP_FILTER_TYPE matcher.FILTER_TYPE
);

CREATE TABLE IF NOT EXISTS PERSON_CLUB (
    PERSON_ID uuid references PERSON,
    CLUB_ID uuid not null,
    IN_FILTER bool default false,
    PRIMARY KEY (CLUB_ID, PERSON_ID)
);

CREATE TABLE IF NOT EXISTS DAY_LIMIT(
    PERSON_ID uuid NOT NULL REFERENCES PERSON,
    DATE date NOT NULL,
    WALK_COUNT int,
    PRIMARY KEY (PERSON_ID, DATE)
);

CREATE TABLE IF NOT EXISTS APPOINTMENT_DETAILS(
    ID uuid PRIMARY KEY,
    CREATED_AT timestamptz,
    STARTS_AT timestamptz,
    UPDATED_AT timestamptz,
    ENDED_AT timestamptz,
    STATUS matcher.APPOINTMENT_STATUS
);

CREATE TABLE IF NOT EXISTS APPOINTMENT(
    APPOINTMENT_ID uuid NOT NULL references APPOINTMENT_DETAILS,
    PERSON_ID uuid NOT NULL references PERSON,
    UNIQUE (APPOINTMENT_ID, PERSON_ID)
);

CREATE TABLE IF NOT EXISTS AVAILABLE_TIME(
    ID uuid PRIMARY KEY,
    PERSON_ID uuid references PERSON,
    TIME_FROM timetz not null,
    TIME_UNTIL timetz not null,
    TIMEZONE varchar not null,
    DATE date not null ,
    FOREIGN KEY (PERSON_ID, DATE) references DAY_LIMIT(PERSON_ID, DATE)
);