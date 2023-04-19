-- Create the auth_database
CREATE DATABASE whatsapp WITH OWNER whatsapp_user;


CREATE TABLE "user"
(
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(256) NOT NULL,
    phone VARCHAR(256) NOT NULL
);


CREATE TABLE "message"
(
    id              SERIAL PRIMARY KEY,
    message         VARCHAR(256) NOT NULL,
    user_id         INTEGER      NOT NULL,
    received_datetime timestamp     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);