# Split Repositories

This directory contains a first-pass extraction of the current monorepo into standalone repositories:

- `gateway-service`
- `common-service`
- `auth-service`
- `agent-service`
- `chat-service`
- `rag-service`
- `usage-service`
- `organization-service`
- `platform-service`
- `frontend`

## What was extracted

- Existing service modules were copied directly from the monorepo.
- `gateway-service` was promoted to a standalone Spring Cloud Gateway repo.
- `organization-service` was extracted from `backend/` and keeps only:
  - `organization`
  - `user`
  - `auth`
  - `config`
  - `common`
- `platform-service` is a standalone extraction of the current `backend/` module and still includes `organization`, `billing`, and `admin`.

## Important notes

- `common-service` is still a shared Java library. The other Java services currently depend on `com.mcs:mcs-common:1.0.0`.
- Before building the other services independently, you should either:
  - publish `common-service` to your package registry, or
  - run `mvn install` in `common-service` locally.
- The frontend `.env.local.example` already points to the gateway port (`8080`) for local integration.
- `gateway-service` still contains the current legacy catch-all `/api/**` forwarding rule. If you later split `billing` or `admin`, that route should be updated again.
- `platform-service` and `organization-service` currently overlap on organization-related code because `platform-service` mirrors the current `backend/` as requested.

## Useful scripts

- `./init-local-repos.sh`
  - Initializes local git repositories for all extracted projects.
- `./create-github-repos.sh <github-owner> [public|private]`
  - Creates GitHub repositories with `gh`, adds `origin`, and pushes the initial import.
