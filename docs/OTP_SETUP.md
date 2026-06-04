# OTP Integration Setup

## Overview
The application supports OTP integration with two implementations:
- **ConsoleOtpService** (dev): Logs OTP codes to console for testing
- **SmsOtpService** (prod): Sends real SMS via configured provider (e.g., Taqnyat)

The system automatically switches between implementations based on the active Spring profile.

## Configuration

### Environment Variables
For production SMS integration:

```bash
# Required
SMS_AUTH_TOKEN=your_sms_api_token

# Optional (with defaults)
SMS_SENDER=GStation
SMS_API_URL=https://api.taqnyat.sa/v1/messages
```

### Configuration Files

**application.yaml** - Base configuration:
```yaml
otp:
  code-length: 4
  ttl-minutes: 5
  resend-cooldown-seconds: 60
  max-attempts-per-day: 5
  sms:
    auth-token: ${SMS_AUTH_TOKEN:}
    sender: ${SMS_SENDER:GStation}
    api-url: ${SMS_API_URL:https://api.taqnyat.sa/v1/messages}
```

**application-dev.yaml** - Development:
```yaml
# No OTP config needed - uses ConsoleOtpService via @Profile("dev")
```

**application-prod.yaml** - Production:
```yaml
# No OTP config needed - uses SmsOtpService via @Profile("prod")
```

## Provider Implementation

### SmsOtpService
Located at: `src/main/java/com/thegamersstation/marketplace/otp/SmsOtpService.java`

Features:
- Active in production profile (`@Profile("prod")`)
- Sends real SMS via configured SMS API
- Uses OkHttp for HTTP requests
- Bilingual OTP messages (Arabic + English)
- Full validation and rate limiting
- Logs all OTP attempts to database
- Caches OTP codes with expiration

### ConsoleOtpService
Located at: `src/main/java/com/thegamersstation/marketplace/otp/ConsoleOtpService.java`

Features:
- Active in dev profile (`@Profile("dev")`)
- Logs OTP codes to console for testing
- No actual SMS sent
- Supports test code "1111" for easy testing
- Same validation logic as production

## How It Works

1. **Profile-Based Loading**: Spring loads the appropriate service based on active profile:
   - `dev` profile → `ConsoleOtpService`
   - `prod` profile → `SmsOtpService`

2. **OTP Flow**:
   - User requests OTP
   - Validation checks (rate limits, daily limits, cooldown)
   - Generate random 4-digit code
   - Store in Caffeine cache (expires in 10 minutes)
   - Send SMS via configured API (or log to console in dev)
   - Log attempt to database

3. **Verification**:
   - User submits OTP code
   - Compare with cached code
   - Invalidate on success
   - Return verification result

## API Request Format

The SmsOtpService sends this payload to the SMS provider:

```json
{
  "sender": "GStation",
  "recipients": ["966580020690"],
  "body": "رمز التحقق الخاص بك هو: 1234\nYour verification code is: 1234"
}
```

With headers:
```
Content-Type: application/json
Authorization: YOUR_SMS_AUTH_TOKEN
```

## Testing

### Development Testing
In dev environment, OTP codes are logged to console:
```
🔐 OTP CODE for +966580020690: 1234 (expires in 5 minutes)
```

You can also use the test code `1111` which always works in dev mode.

### Production Testing
1. Set active profile to production: `SPRING_PROFILE=prod`
2. Set your SMS token: `SMS_AUTH_TOKEN=your_token`
3. Make OTP request to your API
4. Check logs for SMS API responses

## Security Notes

1. **Token Management**: 
   - Never commit `SMS_AUTH_TOKEN` to version control
   - Use environment variables or secret management
   - Rotate tokens periodically

2. **Rate Limiting**:
   - Per-phone: 5 requests/minute
   - Per-IP: 10 requests/minute
   - Daily limit: 5 attempts per phone

3. **Phone Format**:
   - Saudi Arabia numbers: +966XXXXXXXXX
   - Validated using `PhoneValidator`

## Troubleshooting

### OTP not received
- Check SMS API logs in application logs
- Verify `SMS_AUTH_TOKEN` is correct
- Check phone number format
- Verify sender name is approved by SMS provider
- Verify `SMS_API_URL` is correct for your provider

### Wrong implementation loaded
- Check active Spring profile (`SPRING_PROFILE` environment variable)
- Dev profile uses `ConsoleOtpService`
- Prod profile uses `SmsOtpService`
- Check application startup logs for which service is loaded

### Rate limit errors
- Check OTP request logs in database
- Wait for cooldown period (60 seconds)
- Check if daily limit reached

## Monitoring

Monitor these log entries:
- `Sending OTP via SMS to phone: {}` (prod) or `Sending OTP to console for phone: {}` (dev)
- `✅ OTP sent successfully`
- `SMS API error: {}`
- `Failed to send SMS`
- `🔐 OTP CODE for ...` (dev only)
