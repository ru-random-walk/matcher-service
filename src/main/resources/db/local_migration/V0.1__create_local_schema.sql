create schema if not exists matcher;

CREATE EXTENSION if not exists postgis;

UPDATE pg_extension
SET extrelocatable = TRUE
WHERE extname = 'postgis';

ALTER EXTENSION postgis
    SET SCHEMA matcher;