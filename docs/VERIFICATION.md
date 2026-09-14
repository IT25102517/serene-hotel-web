# Verification record

Prepared on 14 September 2026. These checks describe this milestone only.

## Passed

- Spring Boot backend: 38 integration tests; zero failures/errors.
  Reservations 7, events 6, operations 5, marketing 7, finance 6, feedback 7.
- Tests exercise HTTP Basic authentication, wrong-role rejection, customer isolation,
  create/read/update/delete, required fields, stale versions, booking conflicts,
  cancellation slot release, capacity constraints, reversed event times, protected
  tasks, draft visibility, newsletter consent/demo status, inquiry updates,
  duplicate/oversized payments and protected complaint responses.
- React/Vite production build passed before the final print-CSS selector correction.
- Local browser: customer sign-in, availability data and seeded reservation visible.
- All six department accounts opened their own staff workspaces.
- Marketing form saved a new published test package and displayed it in the table.
- Finance invoice detail displayed its computed balance and receipt section.
- Public desktop design visually inspected with supplied logo and pink theme.
- Mobile viewport 390px: public navigation and landing page inspected; finance
  document width was 379px, within the viewport; tables can scroll horizontally.
- Windows Maven launcher tested through `mvnw.cmd -version`, including official
  distribution download and SHA-512 verification.
- Twelve PDFs: six guides of six pages, six viva workbooks of five pages.
  All 66 pages extracted and rendered; contact sheets visually inspected, with
  full-size inspection of the SQL setup page. Each workbook has 20 Q&A prompts.

## Archive verification

Run `tools/package_handoff.py` from this original workspace to create delivery
archives, check member path overlap and simulate six Git branches in a temporary
repository. The distribution `manifest.json` contains the actual result and SHA-256
checksums. The simulation does not commit, reset, push or alter this repository's Git
history. Packaging excludes caches, installed dependencies, build outputs, local
databases, secrets and the source proposal.

## Not verified / deliberately deferred

- No running SQL Server instance was available. SQL Server JDBC configuration and
  bootstrap are provided, but automated tests used H2 compatibility mode.
- No MailerLite credentials were supplied and no real subscriber was sent.
- No live payment gateway, refunds, production authentication or deployment.
- Print CSS reviewed; browser print-to-PDF output has not been exported/inspected.
- Cross-module references and package choices remain manual evaluation inputs.
- PDF instructions do not claim that this is exactly 75% under an official rubric.

The full remaining scope is in SCOPE-AND-API.md and each member guide.
