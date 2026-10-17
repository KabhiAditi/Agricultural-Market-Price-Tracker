#!/usr/bin/env bash
# ==============================================================================
# AMPT Ansible Execution Helper Script (WSL / Linux)
# ==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

export ANSIBLE_CONFIG="$SCRIPT_DIR/ansible.cfg"

echo "=== Running Ansible Syntax Check ==="
ansible-playbook --syntax-check -i inventory.ini site.yml

TARGET="${1:-wsl-node}"
if [ $# -gt 0 ]; then
  shift
fi

echo "=== Executing Playbook on Target: $TARGET ==="
ansible-playbook -i inventory.ini site.yml --limit "$TARGET" "$@"
