-- Add role column to T_USER table
ALTER TABLE T_USER ADD [role] VARCHAR(50) NOT NULL DEFAULT 'user';