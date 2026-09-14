# Six-member handoff

- `00-shared-foundation.zip`: coordinator commits once on main.
- `member-1-reservations.zip`: Amarasekara I.S.Y. · IT25101702.
- `member-2-events.zip`: Bandara U.S.B.N. · IT25103405.
- `member-3-operations.zip`: Wijerathna K.G.C.J. · IT25101522.
- `member-4-marketing.zip`: Indusara L.G.S. · IT25102517.
- `member-5-finance.zip`: Wijesinghe W.A.D.M.C.L. · IT25100607.
- `member-6-feedback.zip`: Gunathilake P.G.K.I. · IT25103600.
- `Serene-complete-project.zip`: combined rehearsal snapshot; do not commit this
  independently on every member branch.
- `Serene-team-handoff.zip`: distribution bundle containing all of the above.

Each member ZIP includes two PDFs: a six-page setup guide and a five-page viva
workbook with twenty questions, model answers and practical drills.

## Coordinator workflow

The coordinator is one of the six members; Member 4 may take this role. No extra
member is required. This working folder already has the integrated implementation.
Use a clean clone/worktree based on remote main for the staged workflow below.
Do not reset or delete this integrated working folder.

1. Extract the shared foundation ZIP into a clean clone of the team repository.
2. Review, commit and push the foundation on main. Share its commit ID.
3. Everyone pulls main and creates `codex/member-N-module` from that commit.
4. Each member extracts only their ZIP into the clone root and commits their owned
   UI, Java, tests and docs. Commands are in their PDF guide.
5. Review and merge six PRs. These patches add disjoint paths and require no central
   route/import edits. Their merge order is immaterial.
6. Pull integrated main, run backend tests, build the frontend and rehearse.

## Ownership contract

```text
frontend/src/features/MODULE/
backend/src/main/java/lk/serene/MODULE/
backend/src/test/java/lk/serene/MODULE/
docs/members/member-N-MODULE/
```

The coordinator owns the root README, frontend shell/API/theme/Workbench,
dependency manifests/lockfiles, backend shared package/configuration, database
bootstrap and team docs. Coordinate shared changes once before dependent work.
Do not use `git add .` without inspecting status.

The Vite glob discovers feature `index.jsx` files. Spring scans packages below
`lk.serene`. A foundation plus any member is buildable. Partial projects may show
placeholders for absent modules; the complete project has all six. Generic CRUD
mechanics are shared, while fields and custom screens are member-owned.

Disjoint archives prevent conflicts from these supplied additions. Later edits,
shared file changes and unrelated work can still conflict. See VERIFICATION.md for
the actual overlap and temporary Git merge checks.
