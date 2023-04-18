CREATE TABLE "responses"
(
    id            integer      not null,
    name          varchar(255) not null,
    response_text varchar(255) not null
);


CREATE TABLE conversation_state
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);


ALTER TABLE "user"
    ADD Column "conversation_state_id" integer DEFAULT 1;

ALTER table "user"
    ADD COLUMN  "email" varchar(255);

ALTER TABLE "user"
    ADD CONSTRAINT "user_conversation_state_id_fkey"
        FOREIGN KEY ("conversation_state_id")
            REFERENCES "conversation_state" ("id");

ALTER TABLE "requested_leave"
    ADD Column "user_email" varchar(255);

ALTER TABLE "requested_leave"
    ADD COLUMN "request_journey_completed_status" boolean DEFAULT false;
