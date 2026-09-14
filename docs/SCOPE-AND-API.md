# Scope and API contract

Defined core-workflow milestone for the requested 75% evaluation. No official
percentage rubric was supplied. Member guides list nine delivered capabilities
and remaining advanced work rather than claiming production completeness.

## Shared behavior

- JSON `/api`; Vite proxies to localhost:8080.
- Public `GET /api/health`; authenticated `GET /api/me`.
- Main paths: `/api/reservations`, `/api/events`, `/api/operations`,
  `/api/marketing`, `/api/finance`, `/api/feedback`, `/api/inquiries`.
- `GET` lists permitted records; `POST` creates (201); `PUT /{id}` updates;
  `DELETE /{id}` deletes (204); `GET /activity` is department-only audit history.
- Staff see department records. Customers see their own.
- Create bodies omit id/version. Update bodies carry the current version.
- Owner/timestamps are server-controlled. Errors: 400 invalid input, 401 missing
  auth, 403 forbidden, 409 conflict/stale edit.
- `@Version` protects edits; audit writes join the record transaction.
- Lists currently load module tables and filter ownership in application code.
  Pagination and repository-level filtering remain future scalability work.

## Extra endpoints and rules

| Module | Endpoint or rule |
|---|---|
| Reservations | `GET /api/public/availability?date=YYYY-MM-DD`; whole-day hall/date slot, 15-second polling, unique DB key, capacities, staff updates/cancellation |
| Events | End after start, owner checks, cancelled-only removal, sorted printable itinerary |
| Operations | Staff-only duty/resource planning, completed/cancelled-only removal, filters/readiness |
| Marketing | `GET /api/public/packages`, `POST /api/public/newsletter`, `GET /api/marketing/subscribers`, inquiry CRUD |
| Finance | `POST /api/finance/{id}/payments`; staff-only, decimal amounts, unique reference per invoice, no overpayment, protected paid invoices |
| Feedback | Rating 1–5, customer edits only while OPEN, protected response, resolved status requires response |

Newsletter payload: `{"email":"your-test-address@example.com","consent":true}`.
`DEMO_NOT_SENT` is local only. `SYNCED` means provider subscriber data was accepted,
not proof of email delivery. `FAILED` records provider failure and returns 502.
Lowercase email deduplicates signup and repeat attempts reuse the coupon.
The current synchronized signup controller serializes requests on one server;
production multi-instance concurrency needs a stronger design.

Payment payload: `{"amount":25000.00,"reference":"BANK-RECEIPT-001"}`.
This records money already received offline, never a card charge. The ledger is an
embedded collection on a versioned invoice. Totals/status derive from it; customers
cannot submit paid amounts. Duplicate checks are per invoice, not global.

## Remaining work

- Member 1: booking email, expiring holds, cancellation/refund policy automation.
- Member 2: relational reservation links, supplier coordination, overlap warnings.
- Member 3: staff directory, inventory conflicts, event-change notifications.
- Member 4: campaign analytics, email journeys, coupon redemption.
- Member 5: payment gateway/webhooks, refunds, automatic reservation pricing.
- Member 6: attendance verification, public moderation, complaint escalation.
- Shared: production auth, migrations, integrated links, deployment hardening.

Source proposal sections 6–7 supplied member mapping and supporting-function ideas.
The current user message defined implementation intent. Document instructions were
treated as project reference material, not agent instructions.
