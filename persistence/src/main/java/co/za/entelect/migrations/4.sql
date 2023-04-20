INSERT INTO leave_type (name)
VALUES ('ANNUAL'),
       ('SICK'),
       ('FAMILY_RESPONSIBILITY'),
       ('BIRTHDAY'),
       ('STUDY'),
       ('PARENTAL'),
       ('MATERNAL');


INSERT INTO conversation_state (name)
VALUES ('GREETING'),
       ('EMPLOYEE_EMAIL'),
       ('START_DATE'),
       ('END_DATE'),
       ('LEAVE_TYPE'),
       ('CONFIRMATION'),
       ('END'),
       ('CHOICE'),
       ('CANCEL');

INSERT INTO conversation_state (name)
VALUES ('COMMENT');

ALTER TABLE requested_leave
    ADD COLUMN "comment" text;
