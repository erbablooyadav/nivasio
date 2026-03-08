# Nivasio 🏠

**Smart Residence Management — WhatsApp-First, Multi-Tenant SaaS**

Manage PGs, hostels, apartments, societies, and co-living spaces from a single platform. Automate service requests via WhatsApp, track everything with a live dashboard.

---

## 🚀 Features

- **WhatsApp Bot** — Residents raise tickets by sending a message
- **Kanban Dashboard** — Full ticket lifecycle (Open → Assigned → In Progress → Done)
- **SLA Engine** — Auto-escalates breached tickets every 5 minutes
- **Multi-Tenant** — AOP-enforced tenant isolation, NEVER accept tenantId from request body
- **OTP-Only Auth** — No passwords. Phone → OTP → JWT (HS512)
- **Refresh Token Rotation** — HttpOnly cookies, blacklist on rotation
- **Role-Based Access** — SUPER_ADMIN, PROPERTY_ADMIN, STAFF, RESIDENT
- **Redis-Powered** — OTP storage, message dedup, token blacklist, rate limiting
- **HMAC Webhooks** — SHA-256 signature verification on all incoming webhooks

---

## 🛠 Tech Stack

### Backend
| Layer | Technology |
|-------|-----------|
| Runtime | Java 17, Spring Boot 3.2 |
| Database | MongoDB 7 |
| Cache | Redis 7 |
| Auth | JWT HS512 (15min access + 7d refresh) |
| Security | AOP tenant isolation, HMAC webhooks |
| Build | Gradle |

### Frontend
| Layer | Technology |
|-------|-----------|
| Framework | React 19 + TypeScript |
| Styling | Tailwind CSS v4 |
| State | Zustand (persisted) |
| API | Axios (auto-refresh) |
| Charts | Recharts |
| Icons | Lucide React |

### DevOps
| Layer | Technology |
|-------|-----------|
| Containers | Docker multi-stage |
| Orchestration | Docker Compose |
| Web Server | Nginx (SPA + API proxy) |

---

## 📁 Project Structure

```
pg-manager-pro/
├── backend/
│   ├── src/main/java/in/nivasio/
│   │   ├── config/          # Security, WebSocket, Redis, Async
│   │   ├── controller/      # 8 REST controllers
│   │   ├── dto/              # 9 DTOs
│   │   ├── exception/        # 5 exceptions + GlobalExceptionHandler
│   │   ├── model/            # 10 MongoDB documents
│   │   ├── repository/       # 10 Spring Data repos
│   │   ├── scheduler/        # SLA breach checker
│   │   ├── security/         # JWT, AOP, HMAC
│   │   ├── service/          # 7 business services
│   │   └── whatsapp/         # Bot conversation service
│   └── build.gradle
├── frontend/
│   ├── src/
│   │   ├── api/              # Axios client (auto-refresh)
│   │   ├── components/       # Sidebar, Layout
│   │   ├── pages/            # 7 pages (Login, Dashboard, Tickets, etc.)
│   │   ├── store/            # Zustand auth store
│   │   ├── types/            # TypeScript interfaces
│   │   └── App.tsx
│   └── package.json
├── docker-compose.yml        # MongoDB + Redis + Backend + Frontend
└── README.md
```

---

## 🚀 Quick Start

### Local Development

```bash
# Backend (requires Java 17 + MongoDB + Redis running)
cd backend
./gradlew bootRun

# Frontend
cd frontend
npm install
npm run dev
```

### Docker

```bash
docker-compose up --build
```

Open http://localhost (frontend) or http://localhost:8080/swagger-ui.html (API docs)

---

## 🔑 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/request-otp` | — | Send OTP to phone |
| POST | `/api/v1/auth/verify-otp` | — | Verify OTP → JWT |
| POST | `/api/v1/auth/register` | — | Register new owner |
| POST | `/api/v1/auth/refresh` | Cookie | Rotate refresh token |
| GET | `/api/v1/dashboard/stats` | JWT | Dashboard metrics |
| GET/POST | `/api/v1/tickets` | JWT | List/Create tickets |
| PUT | `/api/v1/tickets/{id}/status` | JWT | Update ticket status |
| PUT | `/api/v1/tickets/{id}/assign` | ADMIN | Assign to staff |
| GET/POST | `/api/v1/staff` | ADMIN | Staff CRUD |
| GET/POST | `/api/v1/rooms` | JWT | Room CRUD |
| GET | `/api/v1/food-feedback` | JWT | List feedback |
| GET/POST | `/api/v1/webhook` | HMAC | WhatsApp webhook |

---

## 🔐 Security Architecture

1. **JWT HS512** — 15min access tokens, 7-day refresh in HttpOnly cookie
2. **AOP Tenant Isolation** — Every service method validates tenantId matches JWT
3. **HMAC-SHA256** — All webhooks verified before processing
4. **OTP Brute Force** — Max 3 attempts per phone per 30 minutes (Redis)
5. **Token Rotation** — Old refresh tokens blacklisted on use
6. **No Stack Traces** — Production errors return generic messages with error codes

---

## 📜 License

MIT
