-- =====================================================
-- Device Management System - Database Schema
-- PostgreSQL 16
-- =====================================================

-- Users roles
CREATE TABLE IF NOT EXISTS user_roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(20)  UNIQUE NOT NULL,
    description TEXT
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(200),
    phone       VARCHAR(100),
    role_id     INT REFERENCES user_roles(id) ON DELETE SET NULL,
    manager_id          INT REFERENCES users(id) ON DELETE SET NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_login_count INT   NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);




-- Device categories
CREATE TABLE IF NOT EXISTS device_categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Locations / rooms
CREATE TABLE IF NOT EXISTS locations (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    floor       VARCHAR(20),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Devices
CREATE TABLE IF NOT EXISTS devices (
    id              SERIAL PRIMARY KEY,
    code            VARCHAR(50) UNIQUE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    category_id     INT REFERENCES device_categories(id) ON DELETE SET NULL,
    location_id     INT REFERENCES locations(id) ON DELETE SET NULL,
    brand           VARCHAR(100),
    model           VARCHAR(100),
    serial_number   VARCHAR(100),
    status          VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    purchase_date   DATE,
    warranty_expiry DATE,
    purchase_price  NUMERIC(15,2),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Assignments (device allocation)
CREATE TABLE IF NOT EXISTS assignments (
    id              SERIAL PRIMARY KEY,
    device_id       INT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    assigned_to     VARCHAR(100) NOT NULL,
    department      VARCHAR(100),
    assigned_by     INT REFERENCES users(id) ON DELETE SET NULL,
    assigned_date   DATE NOT NULL,
    expected_return DATE,
    returned_date   DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS approvals_status (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(20) UNIQUE NOT NULL,
    description     TEXT
);


CREATE TABLE IF NOT EXISTS approvals_types (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(20) UNIQUE NOT NULL,
    description     TEXT
);

CREATE TABLE IF NOT EXISTS approvals (
    id              SERIAL PRIMARY KEY,
    requester_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id       INT REFERENCES devices(id) ON DELETE SET NULL,
    category_id     INT REFERENCES device_categories(id) ON DELETE SET NULL,
    approval_type   INT NOT NULL REFERENCES approvals_types(id),
    description     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver_id     INT REFERENCES users(id) ON DELETE SET NULL,
    comments        TEXT,
    imported        BOOLEAN NOT NULL DEFAULT FALSE,
    request_date    TIMESTAMP NOT NULL DEFAULT NOW(),
    approval_date   TIMESTAMP
);

-- System logs
CREATE TABLE IF NOT EXISTS system_logs (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50),
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id   INT,
    target_info VARCHAR(255),
    result      VARCHAR(20),
    detail      TEXT,
    ip_address  VARCHAR(50),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_devices_status     ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_category   ON devices(category_id);
CREATE INDEX IF NOT EXISTS idx_devices_location   ON devices(location_id);
CREATE INDEX IF NOT EXISTS idx_assignments_device ON assignments(device_id);
CREATE INDEX IF NOT EXISTS idx_assignments_status ON assignments(status);
CREATE INDEX IF NOT EXISTS idx_logs_created       ON system_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_logs_username      ON system_logs(username);

-- =====================================================
-- Seed data
-- =====================================================

INSERT INTO user_roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('MANAGER', 'Manager with elevated access'),
('IT-STAFF', 'IT staff with technical access'),
('USER',  'Regular user with limited access')
ON CONFLICT (name) DO NOTHING;

-- Default admin user (password: Admin@123)
INSERT INTO users (username, password_hash, full_name, email, role_id, manager_id) VALUES
('admin', '$2a$12$cISauHUWwShuJx8GKQ/IlOn2DP0mhE0zCPXnFNnGxH.doMj3VwXpy', 'Quản trị viên', 'admin@company.com', 1, NULL),
('user1', '$2a$12$dUAGFXybE/A.wEkDJ8D0yem/xO71yOqNwAumycS6r2yCgdWCUYcM6', 'Nguyễn Văn A', 'nguyenvana@company.com', 2, 1)
ON CONFLICT (username) DO NOTHING;

-- Device categories
INSERT INTO device_categories (name, description) VALUES
('Màn hình',       'Monitor, display screen'),
('Chuột',          'Mouse, trackpad'),
('Bàn phím',       'Keyboard'),
('CPU/Mini PC',    'Desktop computer, mini PC, workstation'),
('RAM',            'Memory module'),
('Ổ cứng/SSD',     'Hard disk drive, solid state drive'),
('Camera',         'IP camera, webcam, security camera'),
('Switch/Router',  'Network switch, router, access point'),
('Loa',            'Speaker, soundbar'),
('Headset',        'Headphone, headset, microphone'),
('Máy in',         'Printer, scanner, copier'),
('Máy chiếu',      'Projector, screen'),
('UPS',            'Uninterruptible power supply'),
('Dây cáp/Hub',    'Cable, USB hub, docking station'),
('Khác',           'Other devices')
ON CONFLICT (name) DO NOTHING;

-- Locations
INSERT INTO locations (name, description, floor) VALUES
('Phòng IT',        'Phòng kỹ thuật IT',           'Tầng 1'),
('Phòng Giám đốc',  'VP Giám đốc',                 'Tầng 2'),
('Phòng Kế toán',   'Phòng Kế toán - Tài chính',   'Tầng 2'),
('Phòng Nhân sự',   'Phòng Nhân sự',                'Tầng 2'),
('Phòng Kinh doanh','Phòng Kinh doanh - Sales',     'Tầng 3'),
('Phòng họp A',     'Phòng họp lớn',               'Tầng 1'),
('Phòng họp B',     'Phòng họp nhỏ',               'Tầng 2'),
('Kho',             'Kho thiết bị dự phòng',        'Tầng 1'),
('Sảnh',            'Sảnh lễ tân',                  'Tầng 1'),
('Server Room',     'Phòng máy chủ',               'Tầng 1')
ON CONFLICT (name) DO NOTHING;

-- Sample devices
INSERT INTO devices (code, name, category_id, location_id, brand, model, serial_number, status, purchase_date, warranty_expiry, purchase_price) VALUES
('MH-001', 'Màn hình Dell 24 inch',   1, 1, 'Dell',    'P2422H',       'CN-0P2422H-001', 'IN_USE',    '2023-01-15', '2026-01-15', 4500000),
('MH-002', 'Màn hình LG 27 inch',     1, 2, 'LG',      '27UK850-W',    'LG27UK-00123',   'IN_USE',    '2022-06-01', '2025-06-01', 8900000),
('CH-001', 'Chuột Logitech MX Master',2, 1, 'Logitech','MX Master 3',  'LGT-MX3-001',    'IN_USE',    '2023-03-10', '2025-03-10', 1800000),
('BF-001', 'Bàn phím cơ Keychron K2', 3, 1, 'Keychron','K2 Pro',       'KCH-K2P-001',    'AVAILABLE', '2023-05-01', '2025-05-01', 2200000),
('PC-001', 'Mini PC Dell OptiPlex',   4, 3, 'Dell',    'OptiPlex 3090','DO3090-001',      'IN_USE',    '2022-09-01', '2025-09-01', 18000000),
('PC-002', 'Desktop HP ProDesk',      4, 4, 'HP',      'ProDesk 400',  'HP400-002',       'IN_USE',    '2023-02-01', '2026-02-01', 15000000),
('CAM-001','Camera IP Hikvision',      7, 9, 'Hikvision','DS-2CD2143G2','HIK-DS001',       'IN_USE',    '2023-07-01', '2026-07-01', 3200000),
('SW-001', 'Switch Cisco 24 port',    8,10, 'Cisco',   'SG350-28',     'FOC001ABC',       'IN_USE',    '2021-12-01', '2024-12-01', 12000000),
('LOA-001','Loa Jabra Speak 710',     9, 6, 'Jabra',   'Speak 710',    'JAB710-001',      'AVAILABLE', '2023-08-01', '2025-08-01', 6500000),
('IN-001', 'Máy in HP LaserJet',     11, 3, 'HP',      'LaserJet Pro M404n','HP404-001',  'IN_USE',    '2022-11-01', '2025-11-01', 9800000),
('UPS-001','UPS APC 1500VA',         13,10, 'APC',     'BX1500G',      'APC-BX001',       'IN_USE',    '2022-08-01', '2025-08-01', 5500000),
('HDD-001','SSD Samsung 1TB',         6, 8, 'Samsung', '870 EVO 1TB',  'SAM870-001',      'AVAILABLE', '2023-10-01', '2026-10-01', 2800000)
ON CONFLICT (code) DO NOTHING;

-- Sample assignments
INSERT INTO assignments (device_id, user_id, assigned_to, department, assigned_by, assigned_date, status, notes) VALUES
(1, 2, 'IT', 1, '2023-01-20', 'ACTIVE', 'Cấp cho nhân viên IT'),
(2, 3, 'Ban Giám đốc', 1, '2022-06-05', 'ACTIVE', 'Văn phòng giám đốc'),
(3, 4, 'IT', 1, '2023-03-15', 'ACTIVE', NULL),
(5, 5, 'Kế toán', 1, '2022-09-10', 'ACTIVE', 'Máy tính kế toán trưởng'),
(6, 6, 'Nhân sự', 1, '2023-02-05', 'ACTIVE', NULL)
ON CONFLICT DO NOTHING;

--sample approval status
INSERT INTO approvals_status (name, description) VALUES
('PENDING', 'Đang chờ phê duyệt'),
('APPROVED', 'Đã phê duyệt'),
('REJECTED', 'Đã từ chối')
ON CONFLICT (name) DO NOTHING;


--Sample approval types
INSERT INTO approvals_types (name, description) VALUES
('CẤP MỚI', 'Phê duyệt mua thiết bị mới'),
('SỬA CHỮA', 'Phê duyệt sửa chữa thiết bị'),
('THANH LÝ', 'Phê duyệt thanh lý thiết bị')
ON CONFLICT (name) DO NOTHING;

--Sample approvals
INSERT INTO approvals (requester_id, device_id, category_id, approval_type, description, status, approver_id, comments, imported, approval_date) VALUES
(2, NULL, 1, 1, 'Đề xuất cấp thêm 1 màn hình mới cho nhân viên IT', 'APPROVED', 1, 'Đồng ý mua thiết bị mới', FALSE, NOW()),
(2, 3, 2, 2, 'Chuột bị liệt nút phải, cần sửa chữa', 'PENDING', NULL, NULL, FALSE, NULL),
(2, 4, 3, 3, 'Bàn phím hỏng nhiều phím, đề nghị thanh lý', 'REJECTED', 1, 'Không đồng ý thanh lý, đề nghị sửa chữa trước', FALSE, NOW())
ON CONFLICT DO NOTHING;
