# DonorLink — Blood & Organ Donor Finder

A real-time blood and organ donor matching platform built with Spring Boot 3, MySQL, and vanilla HTML/CSS/JS. Designed to address India's critical shortage of 41 lakh blood units annually by connecting willing donors with patients in urgent need — instantly, by proximity.

---

## The Problem

India's healthcare system faces a persistent blood crisis:

- Over **41 lakh units** of blood are required annually; a significant portion goes unmet
- Patients in emergencies rely on phone chains, WhatsApp forwards, and word of mouth to find donors
- There is no structured, real-time system for families or hospitals to find nearby verified donors fast
- Existing platforms do not enforce donor eligibility rules or donation frequency limits

DonorLink addresses all of these directly.

---

## Solution Overview

DonorLink is a full-stack web application that:

- Allows donors to **register, complete a WHO-based eligibility questionnaire, and set their availability**
- Enforces a **3-month cooldown between blood donations** as per NBTC guidelines
- Lets hospitals and families **post urgent requests** that immediately trigger **email notifications to nearby eligible donors** using geo-based search
- Shows **upcoming blood donation camps** and connected hospital blood banks
- Uses the **Haversine formula** for accurate proximity-based donor matching

---

## Features

### Donor Side
- Register with blood group, location, and medical details
- Complete a 9-question eligibility questionnaire (WHO/NBTC guidelines)
- 3-month cooldown validation: donors who donated recently are automatically excluded from searches
- Toggle blood and organ donation availability on/off
- Receive email alerts when a matching request is posted near you

### Hospital / Family Side
- Post urgent blood or organ requests
- System instantly notifies all nearby eligible matching donors by email
- Track request status: Open, Fulfilled, Cancelled

### Search
- Geo-based donor search using the Haversine formula (great-circle distance)
- Filter by blood group, organ type, and search radius (25 / 50 / 100 / 200 km)
- Results sorted by distance ascending

### General
- Live urgency ticker showing all open requests
- Donation camps listing with dates and venues
- Partner hospital blood bank directory with contact details
- JWT-based authentication with role separation: DONOR, HOSPITAL, ADMIN

---

## Eligibility Questionnaire

Based on WHO Blood Donor Selection Guidelines and National Blood Transfusion Council (NBTC) of India criteria. Donors must confirm:

1. No surgery, major dental work, or blood transfusion in the last 6 months
2. No tattoo, piercing, or acupuncture in the last 12 months
3. No chronic illness — HIV, Hepatitis B/C, insulin-dependent diabetes, active cancer
4. Not currently pregnant or breastfeeding
5. No alcohol consumption in the last 24 hours
6. Not on antibiotics, blood thinners, or immunosuppressants
7. Weight 50 kg or above (NBTC minimum)
8. No malaria, typhoid, or dengue in the last 3 months
9. No adverse reactions to previous blood donations

A donor who fails any criterion is not marked eligible and does not appear in searches.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 21 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | MySQL 8 + Spring Data JPA + Hibernate |
| Email | Spring Mail (Gmail SMTP / Mailtrap) |
| Geo Search | Haversine formula via native MySQL query |
| Frontend | HTML5, CSS3, Vanilla JS (served as Spring Boot static resource) |
| Build | Maven |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/donorfinder/
│   │   ├── config/         # Security, JWT, CORS, Async
│   │   ├── controller/     # Auth, Donor, UrgentRequest
│   │   ├── dto/            # Request/response DTOs
│   │   ├── exception/      # Global exception handler
│   │   ├── model/          # User, Donor, UrgentRequest, DonorResponse
│   │   ├── repository/     # JPA repositories with native geo queries
│   │   └── service/        # AuthService, DonorService, UrgentRequestService, EmailService
│   └── resources/
│       ├── static/
│       │   └── index.html  # Frontend SPA
│       └── application.properties
```

---

## Setup

### Prerequisites
- Java 21
- Maven
- MySQL 8
- IntelliJ IDEA (recommended)

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/donor-finder.git
cd donor-finder
```

### 2. Create the database

```sql
CREATE DATABASE donor_finder_db;
```

### 3. Configure credentials

Create `src/main/resources/application-local.properties` (this file is gitignored):

```properties
spring.datasource.password=your_mysql_password
app.jwt.secret=your_random_secret_key_min_32_chars
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password
```

To generate a Gmail App Password: Google Account → Security → 2-Step Verification → App Passwords.

### 4. Activate the local profile in IntelliJ

Run → Edit Configurations → Active profiles → type `local`

### 5. Run

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` in your browser.

---

## API Reference

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register as DONOR or HOSPITAL |
| POST | `/api/auth/login` | Public | Login and receive JWT token |

### Donors
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/donors/profile` | DONOR | Create or update donor profile |
| GET | `/api/donors/profile` | DONOR | Get own donor profile |
| GET | `/api/donors/eligibility` | DONOR | Check current donation eligibility |
| POST | `/api/donors/search/blood` | Authenticated | Search nearby blood donors |
| POST | `/api/donors/search/organ` | Authenticated | Search nearby organ donors |
| PATCH | `/api/donors/availability/blood?available=true` | DONOR | Toggle blood availability |
| PATCH | `/api/donors/availability/organ?available=true` | DONOR | Toggle organ availability |

### Requests
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/requests` | Authenticated | Post urgent blood or organ request |
| GET | `/api/requests/open` | Public | Get all open requests |
| GET | `/api/requests/mine` | Authenticated | Get own posted requests |
| PATCH | `/api/requests/{id}/fulfill` | Authenticated | Mark request as fulfilled |
| PATCH | `/api/requests/{id}/cancel` | Authenticated | Cancel a request |

---

## How Distance is Calculated

The geo-search runs entirely inside MySQL using the **Haversine formula**:

```sql
6371 * acos(
  cos(radians(:lat)) * cos(radians(d.latitude)) *
  cos(radians(d.longitude) - radians(:lon)) +
  sin(radians(:lat)) * sin(radians(d.latitude))
)
```

This computes the great-circle distance between two points on Earth. `6371` is Earth's mean radius in kilometres. Results are filtered by the requested radius and sorted nearest-first. This is straight-line distance, not road distance.

---

## Donor Eligibility Rules

| Rule | Basis |
|---|---|
| Minimum gap between donations | 3 months (NBTC India) |
| Minimum age | 18 years |
| Maximum age | 65 years |
| Minimum weight | 50 kg |
| Chronic illness | Permanent deferral |
| Recent surgery | 6-month deferral |
| Recent tattoo/piercing | 12-month deferral |
| Pregnancy/breastfeeding | Deferred until 6 months post-delivery |
| Recent malaria/typhoid/dengue | 3-month deferral |

---

## Deployment

### Railway (recommended for free hosting)

1. Push to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add a MySQL plugin from the Railway dashboard
4. Set environment variables: `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
5. Railway auto-builds and deploys — your app gets a public URL

The frontend is served from Spring Boot's static folder, so only one deployment is needed.

---

## What Could Be Added Next

- SMS notifications via Twilio (critical for users without reliable email access)
- Admin dashboard for request moderation and donor verification
- Real-time notifications via WebSocket
- Integration with actual hospital blood bank inventory APIs
- Mobile app (React Native or Flutter)
- Road-distance-based search using Google Maps Distance Matrix API

---

## References

- [WHO Blood Donor Selection Guidelines](https://www.who.int/publications/i/item/9789241548519)
- [National Blood Transfusion Council of India — Donor Eligibility Criteria](http://nbtc.naco.gov.in)
- [Ministry of Health — National Blood Policy](https://mohfw.gov.in)
