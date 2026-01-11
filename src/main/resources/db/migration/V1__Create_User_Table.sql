-- Create T_USER table
CREATE TABLE T_USER (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    active BIT NOT NULL,
    created_at DATE,
    updated_at DATE
);

-- Create indexes for better query performance
CREATE INDEX idx_username ON T_USER(username);
CREATE INDEX idx_email ON T_USER(email);

