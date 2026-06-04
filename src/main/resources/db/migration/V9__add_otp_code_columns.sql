-- Add OTP code and expiry columns to support DB-based OTP storage
ALTER TABLE otp_logs ADD COLUMN code VARCHAR(10) NULL;
ALTER TABLE otp_logs ADD COLUMN expires_at TIMESTAMP NULL;

-- Index for efficient OTP verification lookups
CREATE INDEX idx_otp_logs_phone_expires ON otp_logs (phone_number, expires_at);

-- Index for cleanup of expired entries
CREATE INDEX idx_otp_logs_expires_at ON otp_logs (expires_at);
