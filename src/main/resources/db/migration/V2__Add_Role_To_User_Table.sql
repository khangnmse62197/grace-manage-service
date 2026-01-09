-- Add role column to T_USER table
ALTER TABLE T_USER ADD role VARCHAR(50) NOT NULL DEFAULT 'user';

-- Create index for role-based queries
CREATE INDEX idx_role ON T_USER(role);

-- Update existing users with appropriate roles (if any exist)
UPDATE T_USER SET role = 'user' WHERE role IS NULL;

-- Add check constraint to ensure only valid roles
ALTER TABLE T_USER ADD CONSTRAINT chk_role CHECK (role IN ('admin', 'user', 'viewer'));

