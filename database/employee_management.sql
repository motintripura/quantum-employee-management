CREATE DATABASE employee_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE employee_management;

-- =========================================
-- 1. Departments
-- =========================================

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);


-- =========================================
-- 2. Designations
-- =========================================

CREATE TABLE designations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    department_id BIGINT NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_designation_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uk_designation_department
        UNIQUE (name, department_id)
);


-- =========================================
-- 3. Employees
-- =========================================

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_code VARCHAR(50) NOT NULL UNIQUE,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),

    date_of_birth DATE,
    gender VARCHAR(20),

    address TEXT,

    joining_date DATE NOT NULL,

    department_id BIGINT NOT NULL,
    designation_id BIGINT NOT NULL,

    salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    profile_image VARCHAR(255),

    status BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_employee_designation
        FOREIGN KEY (designation_id)
        REFERENCES designations(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_employee_salary
        CHECK (salary >= 0)
);


-- =========================================
-- 4. Users
-- =========================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL,

    employee_id BIGINT UNIQUE,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =========================================
-- 5. Attendance
-- =========================================

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    attendance_date DATE NOT NULL,

    check_in TIME,
    check_out TIME,

    working_hours DECIMAL(5,2) DEFAULT 0.00,
    overtime DECIMAL(5,2) DEFAULT 0.00,

    status VARCHAR(30) NOT NULL,

    remarks VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uk_employee_attendance
        UNIQUE (employee_id, attendance_date)
);


-- =========================================
-- 6. Leave Types
-- =========================================

CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    default_days INT NOT NULL DEFAULT 0,

    status BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);


-- =========================================
-- 7. Leave Requests
-- =========================================

CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    total_days DECIMAL(5,2) NOT NULL,

    reason TEXT,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_leave_type
        FOREIGN KEY (leave_type_id)
        REFERENCES leave_types(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_leave_approver
        FOREIGN KEY (approved_by)
        REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT chk_leave_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_leave_days
        CHECK (total_days > 0)
);


-- =========================================
-- 8. Leave Balances
-- =========================================

CREATE TABLE leave_balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,

    year INT NOT NULL,

    allocated_days DECIMAL(5,2) NOT NULL DEFAULT 0,
    used_days DECIMAL(5,2) NOT NULL DEFAULT 0,
    remaining_days DECIMAL(5,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_balance_leave_type
        FOREIGN KEY (leave_type_id)
        REFERENCES leave_types(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uk_employee_leave_year
        UNIQUE (employee_id, leave_type_id, year)
);


-- =========================================
-- 9. Payroll
-- =========================================

CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    month INT NOT NULL,
    year INT NOT NULL,

    basic_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    house_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    transport_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    medical_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    overtime DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    tax DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    gross_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    payment_date DATE NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uk_employee_payroll
        UNIQUE (employee_id, month, year),

    CONSTRAINT chk_payroll_month
        CHECK (month BETWEEN 1 AND 12),

    CONSTRAINT chk_payroll_year
        CHECK (year >= 2000)
);


-- =========================================
-- 10. Performance Reviews
-- =========================================

CREATE TABLE performance_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    reviewer_id BIGINT NULL,

    review_date DATE NOT NULL,

    rating INT NOT NULL,

    strengths TEXT,
    weaknesses TEXT,
    comments TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_performance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_performance_reviewer
        FOREIGN KEY (reviewer_id)
        REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT chk_performance_rating
        CHECK (rating BETWEEN 1 AND 5)
);

-- Departments

INSERT INTO departments (name, description) VALUES
('IT', 'Information Technology Department'),
('HR', 'Human Resources Department'),
('Finance', 'Finance and Accounts Department'),
('Marketing', 'Marketing Department');


-- Designations

INSERT INTO designations (name, description, department_id)
VALUES
('Software Engineer', 'Software development', 1),
('Senior Software Engineer', 'Senior software development', 1),
('HR Manager', 'Human resource management', 2),
('Accountant', 'Finance and accounting', 3),
('Marketing Executive', 'Marketing operations', 4);


-- Leave Types

INSERT INTO leave_types
(name, description, default_days)
VALUES
('CASUAL', 'Casual Leave', 10),
('SICK', 'Sick Leave', 14),
('ANNUAL', 'Annual Leave', 20),
('EMERGENCY', 'Emergency Leave', 5),
('MATERNITY', 'Maternity Leave', 120),
('OTHER', 'Other Leave', 5);