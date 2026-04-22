#!/usr/bin/env bash

set -euo pipefail

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

  if [[ -d "${repo_dir}/.git" ]]; then
    echo "skip ${repo}: git already initialized"
    continue
  fi

  git -C "${repo_dir}" init -b main >/dev/null
  echo "initialized ${repo}"
done

echo
echo "Local repositories are ready under ${ROOT_DIR}"
echo "If you want initial commits, run git add . && git commit inside each repo."
