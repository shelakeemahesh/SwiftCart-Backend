---
description: Audit a repo against the enterprise standard and raise PRs for every gap
---

# /bootstrap-repo

Follow the global change workflow for every PR below. Do not ask for confirmation between steps.

1. Detect: repos in the workspace, remotes, default branches, stack, existing CI. Inspect the reference repos (HR-Backend, HR-Frontend) read-only.
2. Audit against the repo-standard, github-project, and stack rules. List every gap as a checklist.
3. Create or link the GitHub Project. File an issue for each gap with Project fields set.
4. Open PRs in this order, one concern each, each linked to its issue:
   a. chore/repo-hygiene: .gitignore, remove artifacts and demo data, .editorconfig, .env.example
   b. ci/github-actions: ci.yml, dependabot.yml, codeql.yml, dependency-review.yml
   c. chore/github-templates: PR/issue templates, CODEOWNERS, CONTRIBUTING, SECURITY, CODE_OF_CONDUCT
   d. docs/readme-and-docs: README, docs/, ADR 0001, CHANGELOG
   e. chore/docker: Dockerfile(s), docker-compose.yml
5. Once I have merged (b), apply the repo settings from repo-standard. Until then, list them as pending.
6. Report: PR links, Project link, gaps not fixed (with issue numbers), anything blocked by a hard stop.
