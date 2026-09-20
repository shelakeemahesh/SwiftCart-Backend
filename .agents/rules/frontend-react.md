---
trigger: glob
globs: ["**/*.ts", "**/*.tsx"]
---

# Frontend standards (React + TypeScript)

- Feature-based folders. Strict TypeScript: no any, no @ts-ignore without a linked issue.
- ESLint + Prettier must pass. React Testing Library for components and hooks; test behavior, not implementation.
- One API client with interceptors: attach auth; handle 401 (re-auth) and 403 (forbidden) separately. No hardcoded API URLs or keys; use the bundler's env scheme.
- Route guards mirror backend roles, but the backend is the source of truth for authorization.
- Handle loading, empty, and error states on every data view. Accessible by default (labels, focus, keyboard).
- Dockerfile + nginx for production; pre-commit hooks (lint-staged, commitlint).
