-- Seed default ADMIN role with all 18 permissions
-- Check if ADMIN role already exists before inserting
IF NOT EXISTS (SELECT 1 FROM T_ROLE WHERE name = 'ADMIN')
BEGIN
    INSERT INTO T_ROLE (name, description, created_at, updated_at)
    VALUES ('ADMIN', 'Full system access with all permissions', SYSUTCDATETIME(), SYSUTCDATETIME());
END

-- Get the ADMIN role id
DECLARE @admin_role_id BIGINT;
SELECT @admin_role_id = id FROM T_ROLE WHERE name = 'ADMIN';

-- Insert all 18 permissions for ADMIN role (if not already exists)
IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'view_employees')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'view_employees');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'create_employee')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'create_employee');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'edit_employee')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'edit_employee');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'delete_employee')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'delete_employee');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'view_roles')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'view_roles');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'create_role')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'create_role');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'edit_role')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'edit_role');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'delete_role')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'delete_role');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'view_statistics')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'view_statistics');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'view_check_in_out')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'view_check_in_out');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'manage_check_in_out')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'manage_check_in_out');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'manage_inventory')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'manage_inventory');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'view_notifications')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'view_notifications');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'manage_notifications')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'manage_notifications');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'export_data')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'export_data');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'import_data')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'import_data');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'system_settings')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'system_settings');

IF NOT EXISTS (SELECT 1 FROM T_ROLE_PERMISSION WHERE role_id = @admin_role_id AND permission_code = 'user_management')
    INSERT INTO T_ROLE_PERMISSION (role_id, permission_code) VALUES (@admin_role_id, 'user_management');

