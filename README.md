# Digital Stamp

Multi-shop SaaS for digital QR loyalty / stamp cards. Customers keep a QR wallet. Shop owners scan the code, add stamps, and issue rewards. Super admins manage shops and owners on one platform.

## Folder structure

```text
digital-stamp/
├── backend/                 Spring Boot 3 API (Java 17+)
│   ├── src/main/java/com/digitalstamp/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── util/
│   ├── src/test/
│   ├── pom.xml
│   └── README.md
├── frontend/                React + Vite dashboards
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── database/
│   └── schema.sql           Portable MySQL 8 schema (easy to adapt to PostgreSQL)
├── docker-compose.yml       Local MySQL 8
├── .env.example
├── .gitignore
└── README.md
```

## Setup

### 1. Database

**Option A — Docker**

```bash
docker compose up -d
```

**Option B — local MySQL 8+**

```sql
CREATE DATABASE digital_stamp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

You can also apply `database/schema.sql`. Hibernate `ddl-auto=update` will create/update tables on first boot if they are missing.

### 2. Environment variables

Copy `.env.example` and adjust secrets. For the backend, export them in your shell or IDE run configuration (do not commit real passwords).

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | HMAC secret (64+ characters) |
| `JWT_ACCESS_EXPIRATION_MS` | Access token TTL (default 15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token TTL (default 7 days) |
| `CORS_ORIGINS` | Allowed frontend origins |
| `VITE_API_URL` | Frontend API base (`http://localhost:8080/api`) |

### 3. Backend

Requires **Java 17+** and **Maven 3.9+**.

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

API: `http://localhost:8080/api`

### 4. Frontend

Requires **Node 18+**.

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173`

## Test credentials (seeded on first empty database)

| Role | Email | Password |
|---|---|---|
| Super Admin | `admin@digitalstamp.com` | `Admin@123` |
| Shop owner (Starbucks Pune) | `owner@starbucks-pune.com` | `Owner@123` |
| Shop owner (Cafe Corner) | `owner@cafe-corner.com` | `Owner@123` |
| Shop owner (XYZ Cafe) | `owner@xyz-cafe.com` | `Owner@123` |
| Customer | `neha@example.com` | `Customer@123` |
| Customer | `amit@example.com` | `Customer@123` |
| Customer | `sara@example.com` | `Customer@123` |
| Customer | `vikram@example.com` | `Customer@123` |

Public shop URLs:

- `/shop/starbucks-pune`
- `/shop/cafe-corner`
- `/shop/xyz-cafe`

Disable seeding with `APP_SEED=false`.

## API documentation

All JSON responses use:

```json
{ "success": true, "message": "Success", "data": {}, "timestamp": "..." }
```

### Auth

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | Public (customers) |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/refresh` | Public |
| POST | `/api/auth/logout` | Authenticated |

Login body: `{ "email", "password" }`  
Login data: `{ accessToken, refreshToken, userId, name, email, role, tokenType }`

### Public shops

| Method | Path |
|---|---|
| GET | `/api/shops/public` |
| GET | `/api/shops/slug/{slug}` |
| POST | `/api/shops/slug/{slug}/enroll` (customer) |

### Customer

| Method | Path |
|---|---|
| GET/PUT | `/api/customers/profile` |
| GET | `/api/customers/qr` |
| GET | `/api/customers/loyalty-cards` |
| GET | `/api/customers/rewards` |
| GET | `/api/customers/history` |
| GET | `/api/customers/redemptions` |
| POST | `/api/rewards/{id}/redeem` |

QR payload format: `CUSTOMER:{uuid}` (no password or JWT).

### Shop owner

| Method | Path |
|---|---|
| GET | `/api/owner/dashboard` |
| GET/PUT | `/api/owner/shop` |
| GET | `/api/owner/customers` |
| GET/PUT | `/api/owner/loyalty` |
| GET/POST | `/api/owner/rewards` |
| PUT/DELETE | `/api/owner/rewards/{id}` |
| GET | `/api/owner/transactions` |
| GET | `/api/owner/redemptions` |
| POST | `/api/stamps/scan` `{ "qrPayload" }` |
| POST | `/api/stamps/add` `{ "customerId", "stamps" }` |
| POST | `/api/stamps/reverse` `{ "customerId", "stamps" }` |

### Super admin

| Method | Path |
|---|---|
| GET | `/api/admin/dashboard` |
| GET | `/api/admin/statistics` |
| GET/POST | `/api/admin/shops` |
| PUT | `/api/admin/shops/{id}` |
| PUT | `/api/admin/shops/{id}/activate` |
| PUT | `/api/admin/shops/{id}/deactivate` |
| DELETE | `/api/admin/shops/{id}` |
| GET/POST | `/api/admin/shop-owners` |
| PUT | `/api/admin/shop-owners/{id}` |
| GET | `/api/admin/customers` |
| PUT | `/api/admin/customers/{id}` |
| GET | `/api/admin/transactions` |
| GET | `/api/admin/redemptions` |
| GET/PUT | `/api/admin/settings` |

Send `Authorization: Bearer {accessToken}` on protected routes.

## Deployment

1. Provision MySQL 8 (or later PostgreSQL after swapping the dialect and JDBC driver).
2. Set production env vars: strong `JWT_SECRET`, unique DB password, `CORS_ORIGINS` to your HTTPS frontend origin.
3. Build backend: `cd backend && mvn -DskipTests package` then `java -jar target/digital-stamp-backend-1.0.0.jar`.
4. Build frontend: `cd frontend && npm ci && npm run build`. Serve `frontend/dist` with nginx or any static host, proxy `/api` to the Spring Boot process.
5. Set `spring.jpa.hibernate.ddl-auto=validate` (or `none`) in production after applying `database/schema.sql`.
6. Terminate TLS at a reverse proxy. Do not expose MySQL publicly.

## Known limitations

- Logo upload is a URL string; binary upload storage is not included.
- One loyalty program per shop.
- Camera scanning needs HTTPS (or localhost) and browser camera permission.
- Daily/monthly charts are table-based activity lists rather than a dedicated time-series warehouse.
- Shop delete removes that shop’s cards, stamps, rewards, and redemptions.
