ALTER TABLE "user"
    ADD COLUMN phone_number_id varchar(256) NOT NULL;


ALTER TABLE "message"
    ADD COLUMN message_id varchar(256) NOT NULL DEFAULT 0;


CREATE TABLE leave_type
(
    id   SERIAL PRIMARY KEY,
    name varchar(256) NOT NULL
);


CREATE TABLE requested_leave
(
    id                      SERIAL PRIMARY KEY,
    user_id                 INTEGER,
    leave_type_id           INTEGER,
    start_date              DATE,
    end_date                DATE,
    day_count               INTEGER,
    request_created_date    timestamp,
    request_approved_status bool NOT NULL DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    FOREIGN KEY (leave_type_id) REFERENCES "leave_type" (id)
);
