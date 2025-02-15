-- Добавляем расширение для работы с географическими объектами
CREATE EXTENSION if not exists postgis;

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

CREATE TABLE IF NOT EXISTS PERSON(
    ID uuid PRIMARY KEY,
    FULL_NAME varchar(100),
    GENDER varchar(1),
    AGE int
);

CREATE TABLE IF NOT EXISTS PERSON_CLUB (
    PERSON_ID uuid references PERSON,
    CLUB_ID uuid not null,
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
    STATUS matcher.APPOINTMENT_STATUS,
    APPROXIMATE_LOCATION geometry(POINT, 4326)
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
    DATE date not null,
    LOCATION geometry(POINT, 4326),
    SEARCH_AREA_METERS int default 5000,
    CLUBS_IN_FILTER jsonb,
    FOREIGN KEY (PERSON_ID, DATE) references DAY_LIMIT(PERSON_ID, DATE)
);