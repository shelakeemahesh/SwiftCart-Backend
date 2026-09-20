---
trigger: model_decision
description: Apply when creating, restructuring, or cleaning up a repository, including README, docs, CI workflows, GitHub templates, repo settings, Docker, and project layout.
---

# Enterprise repo standard (NexusHR is the reference)

Reference repos, read-only: github.com/shelakeemahesh/HR-Backend and github.com/shelakeemahesh/HR-Frontend. Mirror their layout and README style; where they lack something below, this list wins.

## Naming and layout
- <Product>-Backend and <Product>-Frontend. If the product is a monorepo, keep it and use /backend and /frontend. Never rename, split, or merge repos without asking.

## Root files
- README.md: summary, CI/coverage/license badges, Mermaid architecture diagram, stack table, features, one-command quick start via docker compose, env var table (no real values), structure, testing, API docs link, deployment, contributing, Project board link
- LICENSE, CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md, CHANGELOG.md (Keep a Changelog + SemVer)
- .editorconfig, .gitattributes, .env.example
- .gitignore: Java/Maven/Gradle, Node, .idea/.vscode, .DS_Store, .env

## .github/
- PULL_REQUEST_TEMPLATE.md
- ISSUE_TEMPLATE/: bug_report.yml, feature_request.yml, config.yml
- CODEOWNERS
- dependabot.yml (maven/gradle, npm, docker, github-actions)
- workflows/: ci.yml (build + lint + test on PR and push), codeql.yml, dependency-review.yml, release.yml (tag vX.Y.Z → GitHub Release with generated notes)

## docs/
architecture.md, adr/0001-record-architecture-decisions.md, api.md (or OpenAPI link), runbook.md, deployment.md, onboarding.md

## Hygiene
Remove committed build artifacts (target/, node_modules/, dist/, build/), OS/IDE files, leftover demo/mock data, and dead code. Ignore them going forward. Commit .agents/ (rules + workflows) so it travels with the repo.

## Repo settings (pre-approved via gh api)
- Protect the default branch: PR required, required status check = CI, conversation resolution, linear history, no force-push, no deletion.
- Required approvals: 0 while I'm the only contributor; raise to 1 when a second collaborator joins.
- Squash merge default; auto-delete merged branches.
- Secret scanning + push protection, Dependabot alerts/security updates, code scanning, where the plan allows.
- Repo description, topics, homepage set.
- Apply branch protection only AFTER the CI workflow is on the default branch, otherwise merges deadlock.
