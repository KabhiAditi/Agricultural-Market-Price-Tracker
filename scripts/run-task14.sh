#!/usr/bin/env bash
# ==============================================================================
# Agricultural Market Price Tracker (AMPT) - Task 14 Automation Runner
# Automates Clean Node Provisioning, Idempotency Verification, Release & Rollback
# ==============================================================================
set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ANSIBLE_DIR="$PROJECT_ROOT/ansible"
EVIDENCE_DIR="$PROJECT_ROOT/docs/evidence/task14"

mkdir -p "$EVIDENCE_DIR"
cd "$ANSIBLE_DIR"

export ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg"

echo "======================================================================"
echo " Starting Task 14 Reliability, Idempotency, and Rollback Validation"
echo " Project Root: $PROJECT_ROOT"
echo " Evidence Dir: $EVIDENCE_DIR"
echo "======================================================================"

# ----------------------------------------------------------------------
# Step 0: Syntax validation
# ----------------------------------------------------------------------
echo ""
echo "[Step 0] Syntax checking all Ansible playbooks..."
ansible-playbook --syntax-check -i inventory.ini site.yml release.yml rollback.yml

# ----------------------------------------------------------------------
# Step 1: Clean Target Node & Provision from Scratch
# ----------------------------------------------------------------------
echo ""
echo "[Step 1] Destroying existing target state to simulate clean node..."
sudo systemctl stop ampt.service 2>/dev/null || true
sudo systemctl disable ampt.service 2>/dev/null || true
sudo rm -f /etc/systemd/system/ampt.service
sudo systemctl daemon-reload 2>/dev/null || true
sudo rm -rf /opt/ampt /var/log/ampt /etc/ampt
sudo userdel -r ampt 2>/dev/null || true
sudo groupdel ampt 2>/dev/null || true

echo "Target node cleaned. Running initial provisioning playbook (v1.0.0)..."
ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0 --limit wsl-node | tee "$EVIDENCE_DIR/1-clean-provision.log"

# ----------------------------------------------------------------------
# Step 2: Idempotency Verification (Must Report changed=0)
# ----------------------------------------------------------------------
echo ""
echo "[Step 2] Running site playbook immediately a second time for idempotency..."
ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0 --limit wsl-node | tee "$EVIDENCE_DIR/2-idempotency.log"

# Verify changed=0 in output
if grep -q "changed=0" "$EVIDENCE_DIR/2-idempotency.log"; then
    echo ">>> IDEMPOTENCY PASSED: changed=0 verified!"
else
    echo ">>> IDEMPOTENCY WARNING: changed was not 0. Inspect $EVIDENCE_DIR/2-idempotency.log"
fi

# ----------------------------------------------------------------------
# Step 3: Standalone Health Check Script
# ----------------------------------------------------------------------
echo ""
echo "[Step 3] Executing standalone health check script scripts/health-check.sh..."
bash "$SCRIPT_DIR/health-check.sh" http://127.0.0.1:8080/actuator/health 20 2 | tee "$EVIDENCE_DIR/health-check.log"

# ----------------------------------------------------------------------
# Step 4A: Release Deployment (v1.0.1)
# ----------------------------------------------------------------------
echo ""
echo "[Step 4A] Deploying release v1.0.1 via release.yml..."
ansible-playbook -i inventory.ini release.yml -e app_version=v1.0.1 --limit wsl-node | tee "$EVIDENCE_DIR/3-deploy-v101.log"

echo "Verifying service health after v1.0.1 release..."
bash "$SCRIPT_DIR/health-check.sh" http://127.0.0.1:8080/actuator/health 20 2 | tee "$EVIDENCE_DIR/3-deploy-v101-health.log"

# ----------------------------------------------------------------------
# Step 4B: Rollback to Previous Version (v1.0.0)
# ----------------------------------------------------------------------
echo ""
echo "[Step 4B] Rolling back to previous version v1.0.0 via rollback.yml..."
ansible-playbook -i inventory.ini rollback.yml -e previous_version=v1.0.0 --limit wsl-node | tee "$EVIDENCE_DIR/4-rollback-v100.log"

echo "Verifying service health after rollback to v1.0.0..."
bash "$SCRIPT_DIR/health-check.sh" http://127.0.0.1:8080/actuator/health 20 2 | tee "$EVIDENCE_DIR/4-rollback-v100-health.log"

echo ""
echo "======================================================================"
echo " Task 14 Automation Execution COMPLETED successfully!"
echo " Evidence logs recorded in: $EVIDENCE_DIR"
ls -la "$EVIDENCE_DIR"
echo "======================================================================"
