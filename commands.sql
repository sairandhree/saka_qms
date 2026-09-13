

CREATE  TABLE qms_checklist.departments (
    id INTEGER PRIMARY KEY,
    dept_name VARCHAR(100),
    manager VARCHAR(100),
    modified_by VARCHAR(50),
    modified_on TIMESTAMP,
    authorised_by VARCHAR(50)
);


CREATE TABLE qms_checklist.employees (
    id INTEGER PRIMARY KEY,
    emp_id INTEGER,
    emp_name VARCHAR(100),
    emp_dept INTEGER,
    modified_by VARCHAR(50),
    modified_on TIMESTAMP,
    authorised_by VARCHAR(50),
    CONSTRAINT fk_emp_dept FOREIGN KEY (emp_dept)
        REFERENCES qms_checklist.departments(id)
);

--DROP TABLE IF EXISTS qms_checklist.name_of_item;
CREATE  TABLE qms_checklist.name_of_item (
    id INTEGER PRIMARY KEY,
    name_of_item TEXT,
    note TEXT,
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
    modified_by VARCHAR(50),
    modified_on TIMESTAMP,
    authorised_by VARCHAR(50)
);
--DROP TABLE IF EXISTS qms_checklist.details;
CREATE TABLE qms_checklist.details (
    id INTEGER PRIMARY KEY,
    details TEXT,
    note TEXT,
    parameters TEXT,
    rel_id INTEGER,
    link TEXT,
    sequence INTEGER,
    passward VARCHAR(50),
    detailes_modified_by VARCHAR(50),
    details_auth_by VARCHAR(50),
    details_modified_on TIMESTAMP,
	 CONSTRAINT fk_rel_id FOREIGN KEY (rel_id)
        REFERENCES qms_checklist.name_of_item(id)
);

ALTER TABLE qms_checklist.name_of_item
ADD COLUMN dept_id INTEGER,
ADD CONSTRAINT fk_dept FOREIGN KEY (dept_id)
    REFERENCES qms_checklist.departments(id);

CREATE TABLE qms_checklist.employee_departments (
    emp_id INTEGER NOT NULL,
    dept_id INTEGER NOT NULL,
    PRIMARY KEY (emp_id, dept_id),
    CONSTRAINT fk_emp FOREIGN KEY (emp_id)
        REFERENCES qms_checklist.employees(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dept FOREIGN KEY (dept_id)
        REFERENCES qms_checklist.departments(id)
        ON DELETE CASCADE
);

alter table qms_checklist.employees
 drop column emp_dept;

ALTER TABLE qms_checklist.employees
ADD COLUMN username VARCHAR(100),
ADD COLUMN password VARCHAR(100),
ADD COLUMN isAdmin BOOLEAN;