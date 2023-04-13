CREATE USER whatsapp_user WITH PASSWORD 'password123';

-- Create the auth_database
CREATE DATABASE auth_database WITH OWNER whatsapp_user;


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
    messageDateTime timestamp     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);



GRANT ALL PRIVILEGES ON DATABASE whatsapp TO whatsapp_user;
GRANT ALL PRIVILEGES ON TABLE "message" TO whatsapp_user;
GRANT ALL PRIVILEGES ON TABLE "user" TO whatsapp_user;
GRANT USAGE,
SELECT
ON SEQUENCE message_id_seq TO whatsapp_user;
GRANT USAGE,
SELECT
ON SEQUENCE user_id_seq TO whatsapp_user;
