-- Create T_ROLE table for role management
CREATE TABLE T_ROLE (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_role_name UNIQUE (name)
);

-- Create T_ROLE_PERMISSION junction table
CREATE TABLE T_ROLE_PERMISSION (
    role_id BIGINT NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    CONSTRAINT pk_role_permission PRIMARY KEY (role_id, permission_code),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES T_ROLE(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_role_name ON T_ROLE(name);
CREATE INDEX idx_role_permission_code ON T_ROLE_PERMISSION(permission_code);

