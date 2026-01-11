-- Create index for role-based queries
CREATE INDEX idx_role ON T_USER(role);

-- Add check constraint to ensure only valid roles
ALTER TABLE T_USER ADD CONSTRAINT chk_role CHECK (role IN ('admin', 'user', 'viewer'));

