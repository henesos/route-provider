-- Cleans test data before each test.
-- TRUNCATE with CASCADE handles FK constraints automatically.
-- RESTART IDENTITY resets auto-increment IDs for predictability.
TRUNCATE TABLE transportations RESTART IDENTITY CASCADE;
TRUNCATE TABLE locations RESTART IDENTITY CASCADE;
