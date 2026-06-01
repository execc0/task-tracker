ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR (255),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'USER';

UPDATE users SET username = 'user' || id,
                password = '$2a$10$placeholder',
                role = 'USER'
WHERE username IS NULL;

ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);

