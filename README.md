# Library Booking System - Backend API

This repository contains the robust Spring Boot backend for the Library Seat Booking System. It provides secure authentication, dynamic seat availability tracking, payment processing via Razorpay, and strict data integrity constraints to handle highly concurrent booking requests.

---

## 🏗 Architecture & Tech Stack

*   **Framework:** Java 21 + Spring Boot 3
*   **Database:** PostgreSQL (with `pg_bitemporal` style exclusion constraints)
*   **Authentication:** Google OAuth 2.0 + Stateless JWT (JSON Web Tokens)
*   **Payment Gateway:** Razorpay SDK (Test Mode)
*   **Security:** Spring Security (CORS enabled, CSRF disabled for stateless JWT)
*   **ORM/Data Access:** Spring Data JPA + Hibernate
*   **Configuration:** `spring-dotenv` for secure environment variable management

### Database Integrity Highlights
To prevent race conditions and double-booking, the database uses advanced PostgreSQL features:
*   **PostgreSQL `tstzrange`:** Time ranges are stored natively in the database.
*   **GiST Exclusion Constraints:** 
    *   `ex_bookings_active_seat_time_range`: Prevents two users from booking the same seat at overlapping times.
    *   `ex_bookings_active_user_time_range`: Prevents a single user from booking multiple overlapping sessions.

---

## 🔐 Security

The application utilizes a hybrid authentication approach:
1.  **OAuth Login:** The frontend retrieves a Google ID token and sends it to the backend.
2.  **Google Verification:** The backend verifies the signature cryptographically with Google's public keys.
3.  **JWT Issuance:** The backend generates its own short-lived stateless JWT signed with a secret key.
4.  **Authorization Filter:** `JwtAuthenticationFilter` intercepts all secured API calls and populates the `SecurityContext` automatically.

---

## 📡 API Endpoints

### Authentication (`/api/auth`)
*   `POST /api/auth/oauth2/google`
    *   **Body:** `{ "idToken": "..." }`
    *   **Action:** Verifies Google token, creates/updates user, returns JWT `accessToken`.

### Bookings (`/api/bookings`)
*(Requires Bearer JWT in Authorization Header)*
*   `GET /api/bookings/availability`
    *   **Query Params:** `zoneId`, `startTime` (ISO-8601), `endTime` (ISO-8601)
    *   **Action:** Returns a list of `seatId`s currently available for the given time range.
*   `POST /api/bookings/hold`
    *   **Body:** `{ "zoneId": 1, "seatId": 1, "startTime": "...", "endTime": "..." }`
    *   **Action:** Creates a temporary `HELD` booking for 10 minutes and returns a fare calculation.
*   `GET /api/bookings/my-bookings`
    *   **Action:** Returns a list of all past, upcoming, and held bookings for the authenticated user.

### Payments (`/api/payments`)
*   `POST /api/payments/create-order` *(Requires JWT)*
    *   **Body:** `{ "bookingId": 1 }`
    *   **Action:** Verifies the `HELD` booking and creates a Razorpay Order ID for frontend checkout.
*   `POST /api/payments/webhook` *(Public, Secured via Signature)*
    *   **Action:** Receives the `payment.captured` event from Razorpay. Cryptographically verifies the signature (`x-razorpay-signature`) and flips the booking status from `HELD` to `CONFIRMED`.

---

## 🚀 Getting Started

### 1. Prerequisites
*   Java 21 & Maven (`mvnw` wrapper included)
*   PostgreSQL running on `localhost:5432`

### 2. Environment Variables
Create a `.env` file in the root directory (alongside `pom.xml`):

```env
DB_URL=jdbc:postgresql://localhost:5432/library_booking
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# 64-character Hex String for JWT HMAC-SHA256 signature
JWT_SECRET_KEY=your_64_char_hex_secret_key
JWT_EXPIRATION=3600000

RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_razorpay_webhook_secret

GOOGLE_CLIENT_ID=your_google_oauth_client_id
```

### 3. Database Setup
Create the database in PostgreSQL:
```sql
CREATE DATABASE library_booking;
```
*(Hibernate will automatically generate the schema upon first boot).*

### 4. Running the Server
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

### 5. Local Webhook Testing
To test Razorpay webhooks locally, expose port 8080 using Pinggy:
```bash
ssh -p 443 -R0:localhost:8080 a.pinggy.io
```
Copy the generated HTTPS link, append `/api/payments/webhook`, and paste it into your Razorpay Webhook settings.
