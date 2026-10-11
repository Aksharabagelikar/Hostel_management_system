-- =====================================================================
-- Hostel Management System - MySQL 8 setup script
-- =====================================================================
-- HOW TO RUN (Windows Command Prompt):
--     mysql -u root -p < database\hostel_management.sql
--
-- NOTE: This script is OPTIONAL. The Spring Boot app creates the database
-- and all tables automatically (spring.jpa.hibernate.ddl-auto=update) and
-- inserts the demo data (users, rooms, students, fees, ...) on first start.
-- Use this file if you want to create the database manually or to show the
-- table structure (ER design) in your viva.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hostel_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostel_management;

-- 1. users: login accounts (role = ADMIN / WARDEN / STUDENT, password stored as BCrypt hash)
CREATE TABLE IF NOT EXISTS users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL
);

-- 2. students: profile of every student (1-to-1 with users)
CREATE TABLE IF NOT EXISTS students (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_no        VARCHAR(30)  NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100),
    phone          VARCHAR(20),
    course         VARCHAR(100),
    year_of_study  INT,
    guardian_name  VARCHAR(100),
    guardian_phone VARCHAR(20),
    address        VARCHAR(255),
    join_date      DATE,
    user_id        BIGINT UNIQUE,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. rooms
CREATE TABLE IF NOT EXISTS rooms (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number  VARCHAR(20) NOT NULL UNIQUE,
    block_name   VARCHAR(50) NOT NULL,
    floor_no     INT,
    capacity     INT NOT NULL,
    room_type    VARCHAR(30),
    monthly_rent DOUBLE
);

-- 4. room_allocations: which student lives in which room (active = currently living there)
CREATE TABLE IF NOT EXISTS room_allocations (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id     BIGINT NOT NULL,
    room_id        BIGINT NOT NULL,
    allocated_date DATE,
    vacated_date   DATE,
    active         BIT NOT NULL,
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_alloc_room    FOREIGN KEY (room_id)    REFERENCES rooms(id)
);

-- 5. fees
CREATE TABLE IF NOT EXISTS fees (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    amount       DOUBLE NOT NULL,
    due_date     DATE,
    paid_date    DATE,
    status       VARCHAR(20),
    description  VARCHAR(150),
    payment_mode VARCHAR(30),
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 6. complaints
CREATE TABLE IF NOT EXISTS complaints (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    category      VARCHAR(50),
    description   VARCHAR(1000),
    status        VARCHAR(20),
    created_at    DATETIME(6),
    admin_remarks VARCHAR(500),
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 7. attendance: one row per student per day
CREATE TABLE IF NOT EXISTS attendance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status          VARCHAR(20),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT uq_att_student_date UNIQUE (student_id, attendance_date)
);

-- 8. notices
CREATE TABLE IF NOT EXISTS notices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    content     VARCHAR(2000),
    posted_by   VARCHAR(100),
    posted_date DATE,
    important   BIT NOT NULL
);

-- 9. room_change_requests
CREATE TABLE IF NOT EXISTS room_change_requests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT NOT NULL,
    current_room_id   BIGINT,
    requested_room_id BIGINT,
    reason            VARCHAR(500),
    status            VARCHAR(20),
    created_at        DATETIME(6),
    admin_remarks     VARCHAR(500),
    CONSTRAINT fk_req_student  FOREIGN KEY (student_id)        REFERENCES students(id),
    CONSTRAINT fk_req_current  FOREIGN KEY (current_room_id)   REFERENCES rooms(id),
    CONSTRAINT fk_req_wanted   FOREIGN KEY (requested_room_id) REFERENCES rooms(id)
);

-- Demo data (users with BCrypt passwords, rooms, students, fees, complaints, notices ...)
-- is inserted automatically by the Java class DataLoader the first time the app starts.
-- To reset everything:  DROP DATABASE hostel_management;  and start the app again.
