# Serene Hotel — wedding reservation system

React + Vite → Spring Boot / Java 17 → Microsoft SQL Server.
White and elegant pink `#e5676d`, using the supplied Serene logo.
SE2030 · 2026-Y2-S1-MLB-B6G2-09 · six members, six owned features.

## Start here

For a full rehearsal use the complete project snapshot. For branch contributions,
commit **00-shared-foundation.zip first**, then let each member add only their
member ZIP on a branch based on that foundation. See [team handoff](docs/TEAM-HANDOFF.md).
Member ZIPs are overlays, not standalone applications.

### Quick local demo (H2, explicitly selected)

Prerequisites: Git, Node.js 22 LTS, JDK 17 or 21. Java 25 was used for local validation.
Windows launcher downloads and checksum-checks Maven if it is missing.

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
```

In another terminal:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Open http://127.0.0.1:5173. The banner identifies demo/H2 mode.
Demo data persists in `backend/data`, excluded from Git and ZIPs.

| Account | Workspace |
|---|---|
| customer, customer2 | Customer records, event plans, invoices, feedback, inquiries |
| reservations | Front office |
| events | Wedding event manager |
| operations | Operations manager and staff |
| marketing | Marketing executive |
| finance | Finance officer |
| feedback | Customer relations |

Demo password: `SereneDemo2026!`. These are predefined evaluation accounts,
with BCrypt password hashes. No self-registration or password reset yet.
The browser stores credentials only in memory; refreshing signs out.

### Microsoft SQL Server (default)

1. Install SQL Server and SSMS; run `database/create-database.sql`.
2. Create a dedicated SQL login, map it to `SereneWeddings`, and grant development
   permissions `db_ddladmin`, `db_datareader`, `db_datawriter`.
3. Confirm SQL authentication, TCP/IP and the instance's actual port.
4. In the backend PowerShell terminal:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=SereneWeddings;encrypt=true;trustServerCertificate=true"
$env:DB_USER="serene_app"
$env:DB_PASSWORD="YOUR_LOCAL_PASSWORD"
$env:DEMO_PASSWORD="YOUR_EVALUATION_PASSWORD"
.\mvnw.cmd spring-boot:run
```

No secrets are supplied. `.env.example` documents variables; PowerShell does not load
it automatically. SQL Server is the default; demo must be explicitly requested.
Hibernate creates/updates tables for this milestone. SQL Server mode does not seed
examples. No running SQL Server service was available here, so **live SQL Server
integration still needs testing on your team's instance**. H2 compatibility mode
is not equivalent to SQL Server validation.

## Folder structure

```text
frontend/src/main.jsx              shared public shell and module discovery
frontend/src/api.js                shared API client
frontend/src/styles.css            shared theme
frontend/src/shared/Workbench.jsx  reusable CRUD form/table
frontend/src/features/MODULE/      member UI and custom components
backend/src/main/java/lk/serene/
  SereneApplication.java
  shared/                         auth, ownership, audit, CRUD, errors
  MODULE/                         entity, repository, controller, demo fixture
backend/src/main/resources/        SQL Server, demo and test profiles
backend/src/test/java/lk/serene/MODULE/
database/                         database bootstrap
docs/members/member-N-MODULE/      setup guide and code viva PDF
```

## Defined milestone

All six modules include core CRUD with appropriate restrictions, role-specific panels
and a supporting function. See [scope and API](docs/SCOPE-AND-API.md). This is a
core-workflow milestone for the requested 75% evaluation, not a measured percentage
against a supplied marking rubric.

- Reservation: availability polling, capacity checks, unique hall/date booking,
  customer creation, staff updates/cancellation.
- Events: activity scheduling, guest requirements, chronological printable itinerary.
- Operations: staff duties, catering/venue/equipment plans, protected task removal.
- Marketing: public packages, promotions, inquiries, MailerLite signup and coupon.
- Finance: invoices, verified offline installments, balances, printable receipts.
- Feedback: reviews/complaints, ratings, staff resolution, complaint tracking.

Cross-module reference IDs are manual text, not relational foreign keys. Package
choices are evaluation constants, not synchronized with marketing pricing. Demo
prices, names and capacities are illustrative, not verified hotel inventory.

## MailerLite — Member 4

Set `MAILERLITE_API_KEY` and `MAILERLITE_GROUP_ID` in the backend environment and
restart. The server uses the [official subscriber API](https://developers.mailerlite.com/api/subscribers).
The secret never enters frontend source. Consent is required and timestamped.
Without keys: local record + `DEMO_NOT_SENT`, explicitly no email sent.
With keys: upsert into your group; require a provider subscriber ID; report `SYNCED`
or `FAILED`. Retrying reuses the code. SYNCED is not proof of email delivery.

The 10% code is shown for front-office approval. Email journeys and automatic invoice
redemption remain future work. No real subscription was sent and no live provider
integration was verified during this build. Before production add rate limits,
abuse controls, appropriate opt-in and consent lifecycle handling, and offer terms.

## Verification

```powershell
cd backend
.\mvnw.cmd test
cd ../frontend
npm.cmd run build
```

Prepared project: 38 API integration tests passed, frontend production build passed.
Tests cover roles, ownership, CRUD, validation, stale edits, booking conflicts,
protected deletions, duplicate payment references and overpayments. Browser and
archive checks are recorded in `docs/VERIFICATION.md`.

## Remaining shared work

Production registration/reset/profile management; HTTPS and hardened auth; database
migrations/backups; real payment gateway and signed webhooks; booking emails;
automatic cross-module links and pricing; live SQL Server and MailerLite verification.
The server binds to localhost. Stateless Basic auth with disabled CSRF is a local
evaluation choice, not a deployment security design. No deployment, remote pushes,
or messages to group members were made.
