# PG Manager Pro 🏠

**WhatsApp-Powered Facility & Service Management SaaS**

A multi-tenant SaaS platform that allows PG owners, hostels, and residential facilities to automate daily service requests via WhatsApp.

---

## 🚀 Features

- **WhatsApp Bot** — Tenants raise tickets via WhatsApp interactive buttons
- **Ticket Management** — Auto-generated IDs (HK-0803-0001), SLA tracking, priority tagging
- **Live Dashboard** — Kanban board with real-time WebSocket updates
- **Staff Management** — Department-based assignment, workload tracking
- **Thermal Printing** — ESC/POS integration for ticket receipts
- **Food Feedback** — Separate module for meal complaints and requests
- **Multi-Tenant** — Shared DB with tenantId isolation, zero data leakage
- **JWT Auth** — Role-based access (Super Admin, Tenant Admin, Staff, Tenant)
- **QR Codes** — Per-room QR codes for quick ticket raising
- **Billing** — Razorpay subscription management
- **PWA** — Works like native app on mobile, offline support

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3.x |
| Database | MongoDB |
| Frontend | React + Vite (PWA) |
| WhatsApp | Meta Cloud API (mocked for dev) |
| Auth | Spring Security + JWT |
| Realtime | WebSocket (STOMP) |
| QR Code | ZXing |
| Billing | Razorpay |
| Deployment | Docker + docker-compose |

---

## 📁 Project Structure

```
pg-manager-pro/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/pgmanager/
│   │   ├── config/             # Security, WebSocket, CORS
│   │   ├── controller/         # REST APIs (8 controllers)
│   │   ├── service/            # Business logic (7 services)
│   │   ├── repository/         # MongoDB repositories
│   │   ├── model/              # MongoDB documents (8 models)
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── security/           # JWT filter, token provider
│   │   ├── whatsapp/           # Bot engine + WA service
│   │   ├── scheduler/          # SLA breach checker
│   │   └── exception/          # Global error handling
│   ├── build.gradle
│   └── Dockerfile
├── frontend/                   # React PWA
│   ├── src/
│   │   ├── pages/              # Dashboard, Tickets, Staff, Rooms...
│   │   ├── components/         # Sidebar, Layout
│   │   ├── context/            # AuthContext
│   │   ├── api/                # API client with JWT handling
│   │   └── index.css           # Design system
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🏃 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MongoDB (local or Atlas)

### Backend
```bash
cd backend
# Set MongoDB URI in application.yml or environment
./gradlew bootRun
# Server starts at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App starts at http://localhost:5173
```

### Docker (Full Stack)
```bash
cp .env.example .env
# Edit .env with your WhatsApp API credentials
docker-compose up -d
# Frontend: http://localhost
# Backend: http://localhost:8080
# MongoDB: localhost:27017
```

---

## 🔑 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new PG (creates tenant + admin) |
| POST | `/api/v1/auth/login` | Login with email/phone + password |
| POST | `/api/v1/auth/refresh` | Refresh JWT token |
| GET | `/api/v1/dashboard/stats` | Dashboard statistics |
| GET/POST | `/api/v1/tickets` | List/Create tickets |
| PUT | `/api/v1/tickets/{id}/status` | Update ticket status |
| PUT | `/api/v1/tickets/{id}/assign` | Assign ticket to staff |
| GET/POST | `/api/v1/staff` | List/Create staff |
| GET/POST | `/api/v1/rooms` | List/Create rooms |
| GET | `/api/v1/food-feedback` | List food feedback |
| GET/POST | `/api/v1/webhook` | WhatsApp webhook |
| GET | `/health` | Health check |

---

## 👥 Roles & Permissions

| Feature | Super Admin | Tenant Admin | Staff | Tenant |
|---------|:-:|:-:|:-:|:-:|
| View all tenants | ✅ | ❌ | ❌ | ❌ |
| Manage rooms/staff | ✅ | ✅ | ❌ | ❌ |
| View all tickets | ✅ | ✅ | Own dept | Own only |
| Update ticket status | ✅ | ✅ | ✅ | ❌ |
| View reports | ✅ | ✅ | ❌ | ❌ |
| Raise ticket | ✅ | ✅ | ✅ | ✅ (via WA) |

---

## 💰 Pricing Tiers

| Tier | Price | Hosting |
|------|-------|---------|
| Tier 0 — Serverless | FREE | Firebase |
| Tier 1 — Self-Host | FREE | Your server |
| Tier 2 — Basic Cloud | ₹499/mo | Our cloud |
| Tier 2 — Standard | ₹999/mo | Our cloud |
| Tier 2 — Pro | ₹1,499/mo | Our cloud |
| Tier 3 — Enterprise | ₹10,000+/mo | Dedicated |

---

## 📋 Environment Variables

```env
MONGODB_URI=mongodb://localhost:27017/pgmanager
JWT_SECRET=your-secret-key
WA_VERIFY_TOKEN=your-webhook-verify-token
WA_ACCESS_TOKEN=your-meta-access-token
WA_PHONE_NUMBER_ID=your-phone-number-id
CORS_ORIGINS=http://localhost:5173
```

---

## 🧪 Development

### WhatsApp Mock Mode
The app starts with `app.whatsapp.mock-mode=true` by default. WhatsApp messages are logged to console instead of calling Meta API. Set to `false` in production with real Meta API credentials.

### SLA Checker
Runs every 5 minutes automatically. Configurable via `app.sla.default-threshold-minutes` (default: 120 minutes = 2 hours).

---

Built with ❤️ for PG/Hostel owners across India.
