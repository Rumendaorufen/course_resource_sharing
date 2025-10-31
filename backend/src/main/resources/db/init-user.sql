-- 密码是 admin123
INSERT INTO user (username, password, role, real_name, email, enabled, created_time, updated_time)
VALUES ('admin', '$2a$10$rTm8zfHG7RYdXRqPHI/DPOJvFZcB6YHZeXqxP.dxJXpxeQhZOvUOK', 'ADMIN', '管理员', 'admin@example.com', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role),
    real_name = VALUES(real_name),
    email = VALUES(email),
    enabled = VALUES(enabled),
    updated_time = NOW();
