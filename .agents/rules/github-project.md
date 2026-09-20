---
trigger: model_decision
description: Apply when creating, linking, or updating a GitHub Project board, or when planning and triaging work as issues.
---

# GitHub Projects (v2)

- One Project per product, linked to all its repos (owner: my GitHub user unless the repos belong to an org).
- Run gh project list. If one exists for the product, link the repos to it (gh project link). Otherwise create it ("<Product> — Engineering") and link. If gh lacks the scope: gh auth refresh -s project.
- Fields: Status (Backlog / Ready / In progress / In review / Done), Priority (P0–P3), Type (Feature / Bug / Tech debt / Chore / Docs), Area (Backend / Frontend / Infra / Docs), Estimate, Iteration.
- Views: Board by Status, Backlog table, Roadmap by Iteration.
- Enable the built-in workflows (auto-add new issues/PRs; closed/merged → Done). Anything not scriptable through gh or the API: give me exact UI click steps, don't stall.
- New issues get labels and Project fields set at creation.
- Use milestones for releases (v0.1.0, v0.2.0 ...).
