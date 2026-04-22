#!/usr/bin/env bash

set -euo pipefail

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is not installed."
  echo "Install it first: https://cli.github.com/"
  exit 1
fi

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <github-owner> [public|private]"
  exit 1
fi

OWNER="$1"
VISIBILITY="${2:-private}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPOS=(
  "gateway-service"
  "common-service"
  "auth-service"
  "agent-service"
  "chat-service"
  "rag-service"
  "usage-service"
  "organization-service"
  "platform-service"
  "frontend"
)

for repo in "${REPOS[@]}"; do
  repo_dir="${ROOT_DIR}/${repo}"
  if [[ ! -d "${repo_dir}" ]]; then
    echo "skip ${repo}: directory not found"
    continue
  fi

  if [[ ! -d "${repo_dir}/.git" ]]; then
    git -C "${repo_dir}" init -b main >/dev/null
  fi

  if ! git -C "${repo_dir}" rev-parse --verify HEAD >/dev/null 2>&1; then
    git -C "${repo_dir}" add .
    git -C "${repo_dir}" commit -m "Initial import from monorepo" >/dev/null
  fi

  if git -C "${repo_dir}" remote get-url origin >/dev/null 2>&1; then
    echo "skip ${repo}: origin already exists"
    continue
  fi

  gh repo create "${OWNER}/${repo}" "--${VISIBILITY}" --source "${repo_dir}" --remote origin --push
done
