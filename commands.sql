-- Initial schema for the QMS application.
-- The name_of_item and details ID sequences are intentionally not created here.
-- Load data into those tables first, then create and configure their sequences.

CREATE SCHEMA IF NOT EXISTS qms_checklist;

CREATE SEQUENCE qms_checklist.departments_id_seq;


CREATE TABLE qms_checklist.departments (
                                           id INTEGER NOT NULL DEFAULT nextval('qms_checklist.departments_id_seq'),
                                           dept_name VARCHAR(100),
                                           modified_by VARCHAR(50),
                                           modified_on TIMESTAMP,
                                           authorised_by VARCHAR(50),
                                           CONSTRAINT pk_departments PRIMARY KEY (id)
);


ALTER SEQUENCE qms_checklist.departments_id_seq
    OWNED BY qms_checklist.departments.id;

insert into qms_checklist.departments(dept_name) values ('Finances and Accounts');
insert into qms_checklist.departments(dept_name) values ('Engineering Design');
insert into qms_checklist.departments(dept_name) values ('Electrical and Instrumentation');
insert into qms_checklist.departments(dept_name) values ('Installation/ Site activity');
insert into qms_checklist.departments(dept_name) values ('Commissioning');
insert into qms_checklist.departments(dept_name) values ('Quality Assurance');
insert into qms_checklist.departments(dept_name) values ('Production');
insert into qms_checklist.departments(dept_name) values ('Stores');
insert into qms_checklist.departments(dept_name) values ('Marketing and Sales ');
insert into qms_checklist.departments(dept_name) values ('Processes');
insert into qms_checklist.departments(dept_name) values ('Human Resources');
insert into qms_checklist.departments(dept_name) values ('Office Administration');


--drop table qms_checklist.employees CASCADE;
CREATE SEQUENCE qms_checklist.employees_id_seq;
CREATE TABLE qms_checklist.employees (
                                         id INTEGER NOT NULL DEFAULT nextval('qms_checklist.employees_id_seq'),
                                         emp_id INTEGER,
                                         emp_name VARCHAR(100) UNIQUE,
                                         username VARCHAR(100) UNIQUE,
                                         password VARCHAR(100),
                                         isadmin BOOLEAN,
                                         is_department_head BOOLEAN,
                                         isdeleted BOOLEAN DEFAULT FALSE,
                                         modified_by VARCHAR(50),
                                         modified_on TIMESTAMP,
                                         authorised_by VARCHAR(50),
                                         CONSTRAINT pk_employees PRIMARY KEY (id)
);

ALTER SEQUENCE qms_checklist.employees_id_seq
    OWNED BY qms_checklist.employees.id;

INSERT INTO qms_checklist.employees (emp_id,emp_name,username,password,isadmin)
VALUES (1,'Anand Thigale','anand','$2a$10$rCCT1yBmksJ6xYYSKGt2NOtyqHDXPNFS1rMyg2HCUJwISQS3IB5TO',true);
--
-- -- Keep the primary administrator from being deleted, including soft deletes.
-- CREATE OR REPLACE FUNCTION qms_checklist.prevent_protected_employee_delete()
-- RETURNS TRIGGER
-- LANGUAGE plpgsql
-- AS $$
-- BEGIN
--     IF OLD.id = 1
--        AND lower(COALESCE(OLD.username, '')) = 'anand'
--        AND (
--            TG_OP = 'DELETE'
--            OR (TG_OP = 'UPDATE' AND NEW.isdeleted IS TRUE)
--        ) THEN
--         RAISE EXCEPTION 'Employee id 1 (anand) cannot be deleted';
--     END IF;
--
--     IF TG_OP = 'DELETE' THEN
--         RETURN OLD;
--     END IF;
--     RETURN NEW;
-- END;
-- $$;
--
-- DROP TRIGGER IF EXISTS protect_anand_employee ON qms_checklist.employees;
-- CREATE TRIGGER protect_anand_employee
--     BEFORE DELETE OR UPDATE OF isdeleted
--     ON qms_checklist.employees
--     FOR EACH ROW
--     EXECUTE FUNCTION qms_checklist.prevent_protected_employee_delete();
--


CREATE TABLE qms_checklist.employee_departments (
                                                    emp_id INTEGER NOT NULL,
                                                    dept_id INTEGER NOT NULL,
                                                    CONSTRAINT pk_employee_departments PRIMARY KEY (emp_id, dept_id),
                                                    CONSTRAINT fk_employee_departments_employee
                                                        FOREIGN KEY (emp_id)
                                                            REFERENCES qms_checklist.employees(id)
                                                            ON DELETE CASCADE,
                                                    CONSTRAINT fk_employee_departments_department
                                                        FOREIGN KEY (dept_id)
                                                            REFERENCES qms_checklist.departments(id)
                                                            ON DELETE CASCADE
);

INSERT INTO qms_checklist.employee_departments values(1,1);
INSERT INTO qms_checklist.employee_departments values(1,2);
INSERT INTO qms_checklist.employee_departments values(1,3);
INSERT INTO qms_checklist.employee_departments values(1,4);
INSERT INTO qms_checklist.employee_departments values(1,5);
INSERT INTO qms_checklist.employee_departments values(1,6);
INSERT INTO qms_checklist.employee_departments values(1,7);
INSERT INTO qms_checklist.employee_departments values(1,8);
INSERT INTO qms_checklist.employee_departments values(1,9);
INSERT INTO qms_checklist.employee_departments values(1,10);
INSERT INTO qms_checklist.employee_departments values(1,11);
INSERT INTO qms_checklist.employee_departments values(1,12);



CREATE TABLE qms_checklist.name_of_item (
                                            id INTEGER PRIMARY KEY,
                                            name_of_item TEXT,
                                            note TEXT,
                                            dept_id INTEGER,
                                            sequence VARCHAR(50),
                                            code1 VARCHAR(50),
                                            code2 VARCHAR(50),
                                            code3 VARCHAR(50),
                                            code4 VARCHAR(50),
                                            code5 VARCHAR(50),
                                            cdinf1 TEXT,
                                            cdinf2 TEXT,
                                            cdinf3 TEXT,
                                            cdinf4 TEXT,
                                            cdinf5 TEXT,
                                            isdeleted BOOLEAN DEFAULT FALSE,
                                            modified_by VARCHAR(50),
                                            modified_on TIMESTAMP,
                                            authorised_by VARCHAR(50),
                                            CONSTRAINT fk_item_department
                                                FOREIGN KEY (dept_id)
                                                    REFERENCES qms_checklist.departments(id)
);

-- Create a sequence
CREATE SEQUENCE qms_checklist.items_id_seq;

-- Set the sequence to start after the current max id
SELECT setval('qms_checklist.items_id_seq',
              COALESCE((SELECT MAX(id) FROM qms_checklist.name_of_item), 0) + 1,
              false);

-- Alter the column to use the sequence by default
ALTER TABLE qms_checklist.name_of_item
    ALTER COLUMN id SET DEFAULT nextval('qms_checklist.items_id_seq');


CREATE TABLE qms_checklist.details (
                                       id INTEGER PRIMARY KEY,
                                       details TEXT,
                                       note TEXT,
                                       parameters TEXT,
                                       rel_id INTEGER,
                                       link TEXT,
                                       sequence INTEGER,
                                       passward VARCHAR(50),
                                       isdeleted BOOLEAN DEFAULT FALSE,
                                       modified_by VARCHAR(50),
                                       modified_on TIMESTAMP,
                                       authorised_by VARCHAR(50),
                                       CONSTRAINT fk_detail_item
                                           FOREIGN KEY (rel_id)
                                               REFERENCES qms_checklist.name_of_item(id)
);


-- Create a sequence
CREATE SEQUENCE qms_checklist.details_id_seq;

-- Set the sequence to start after the current max id
SELECT setval('qms_checklist.details_id_seq',
              COALESCE((SELECT MAX(id) FROM qms_checklist.details), 0) + 1,
              false);

-- Alter the column to use the sequence by default
ALTER TABLE qms_checklist.details
    ALTER COLUMN id SET DEFAULT nextval('qms_checklist.details_id_seq');


CREATE INDEX idx_name_of_item_department
    ON qms_checklist.name_of_item(dept_id);

CREATE INDEX idx_details_related_item
    ON qms_checklist.details(rel_id);
