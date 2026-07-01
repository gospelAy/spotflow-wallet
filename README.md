# Spotflow Wallet Service

Backend service for a gaming/tournament app that handles wallet funding (Pay-In) and cash prize withdrawals (Payout) using the Spotflow payments API.

## Tech Stack

- Java 17
- Spring Boot 3.5
- PostgreSQL
- Flyway
- OpenFeign
- Lombok

## Project Structure

```
com.gospelanyanwu.spotflowwallet
├── config
├── model
├── repository
├── dto
│   ├── request
│   └── response
├── service
├── controller
└── exception
```

## Getting Started

### Prerequisites

- Java 17
- PostgreSQL
- Maven

### Setup

Create the database:

```
createdb spotflow_wallet
```

Set environment variables:

```
SPOTFLOW_SECRET_KEY=your_secret_key
SPOTFLOW_WEBHOOK_SECRET=your_webhook_secret
SPOTFLOW_MAIN_ACCOUNT_NUMBER=your_main_account_number
```

Run the application:

```
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | /wallet/fund | Generate a dynamic account for wallet funding |
| POST | /wallet/withdraw | Withdraw funds to a bank account |
| POST | /webhooks/spotflow | Receive Spotflow payment notifications |

## Webhook Testing (local)

1. Install [ngrok](https://ngrok.com/).
2. Run:

   ```
   ngrok http 8085
   ```

3. Update the Spotflow dashboard webhook URL to:

   ```
   https://your-ngrok-url/webhooks/spotflow
   ```

## Running Tests

```
mvn test
```

## Architecture Notes

- Money is stored as `NUMERIC(19,4)` in the database, never floats.
- Webhook idempotency is handled via a unique database constraint on `webhook_event_id`.
- A scheduled reconciliation job runs every 15 minutes to check transactions stuck as `PENDING` for over an hour.
- Signature verification is implemented but commented out for local testing - re-enable the `WebhookController` signature check before deploying to production.
