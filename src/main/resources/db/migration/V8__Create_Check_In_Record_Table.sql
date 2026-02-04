-- Create T_CHECK_IN_RECORD table for attendance tracking

CREATE TABLE T_CHECK_IN_RECORD (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL, -- 'IN' or 'OUT'
    timestamp DATETIME2 NOT NULL,
    latitude FLOAT,
    longitude FLOAT,
    accuracy FLOAT,
    address NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_check_in_record_user FOREIGN KEY (user_id) REFERENCES T_USER(id) ON DELETE CASCADE
);

-- Add indexes for common queries
CREATE INDEX idx_check_in_record_user_id ON T_CHECK_IN_RECORD(user_id);
CREATE INDEX idx_check_in_record_timestamp ON T_CHECK_IN_RECORD(timestamp);
CREATE INDEX idx_check_in_record_user_timestamp ON T_CHECK_IN_RECORD(user_id, timestamp);
