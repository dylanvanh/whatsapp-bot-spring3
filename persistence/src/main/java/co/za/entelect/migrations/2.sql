ALTER TABLE "user"
    ADD COLUMN phone_number_id varchar(256) NOT NULL;


ALTER TABLE "message"
    ADD COLUMN message_id varchar(256) NOT NULL DEFAULT 0;
