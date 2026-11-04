-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS complaint_system_db;
USE complaint_system_db;

-- 2. Users Table (Stores both Students and Staff)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    college_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Complaints Table (Handles both Public and Private issues)
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    student_sem VARCHAR(10) NOT NULL,
    student_dept VARCHAR(50) NOT NULL,
    target_category VARCHAR(50) NOT NULL,
    area_or_room VARCHAR(80) NOT NULL,
    visibility VARCHAR(15) NOT NULL DEFAULT 'PUBLIC',
    urgency VARCHAR(15) NOT NULL,
    description TEXT NOT NULL,
    image_path VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    votes_count INT DEFAULT 0,
    staff_remarks TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Complaint Votes Table (Prevents duplicate voting by the same student)
CREATE TABLE IF NOT EXISTS complaint_votes (
    vote_id INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id INT NOT NULL,
    student_id INT NOT NULL,
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_student_vote (complaint_id, student_id),
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Insert Sample Demo Accounts for Testing
INSERT INTO users (full_name, email, password_hash, college_name, role) 
VALUES ('Main Maintenance Staff', 'staff@college.edu', 'staff123', 'College of Engineering', 'staff')
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO users (full_name, email, password_hash, college_name, role) 
VALUES ('John Doe', 'student@college.edu', 'student123', 'College of Engineering', 'student')
ON DUPLICATE KEY UPDATE email=email;