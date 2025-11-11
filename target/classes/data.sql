-- Delete all entries in the table
DELETE FROM users;

-- Delete rows where all columns are NULL (if applicable)
DELETE FROM users WHERE username IS NULL AND password IS NULL AND email IS NULL AND credit_score IS NULL;
-- Delete all entries in the table
DELETE FROM accounts;
-- Delete rows where all columns are NULL (if applicable)
DELETE FROM accounts WHERE account_number IS NULL AND balance IS NULL AND user_id IS NULL;

