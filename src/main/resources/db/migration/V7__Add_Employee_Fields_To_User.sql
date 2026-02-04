-- Add Employee Management columns to T_USER

ALTER TABLE T_USER ADD date_of_birth DATE;
ALTER TABLE T_USER ADD role_id BIGINT;
ALTER TABLE T_USER ADD last_check_in_time DATETIME2;
ALTER TABLE T_USER ADD last_check_out_time DATETIME2;

-- Add Foreign Key constraint to T_ROLE
ALTER TABLE T_USER ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES T_ROLE(id);
