# ArgoCD PostgreSQL Migration Guide: Bitnami → CloudPirates

This guide covers migrating PostgreSQL from Bitnami to CloudPirates in ArgoCD-managed deployments.

> ⚠️ **Requires downtime**. Schedule a maintenance window.

## Prerequisites

- ArgoCD access with permissions to manage the application
- `kubectl` access to the cluster
- Backup storage available

## Configuration Variables

```bash
export NAMESPACE="your-namespace"
export RELEASE_NAME="your-release"
export POSTGRES_POD="${RELEASE_NAME}-postgresql-0"
export PG_USER="postgres"
export PG_PASSWORD="your-password"
export BACKUP_FILE=~/backup_$(date +%Y%m%d_%H%M%S).sql
```

---

## Step 1: Backup Database

```bash
# Create backup
kubectl exec -n ${NAMESPACE} ${POSTGRES_POD} -- bash -c \
  "PGPASSWORD=${PG_PASSWORD} pg_dumpall -U ${PG_USER}" > ${BACKUP_FILE}

# Verify backup
grep -q "PostgreSQL database dump" ${BACKUP_FILE} && echo "✅ Backup valid"
```

---

## Step 2: Disable ArgoCD Auto-Sync

**Option A: Via ArgoCD UI**
1. Open your Application in ArgoCD
2. Click "App Details" → "Sync Policy"
3. Disable "Auto-Sync"

**Option B: Via CLI**
```bash
argocd app set <app-name> --sync-policy none
```

---

## Step 3: Delete Old PostgreSQL PVC

ArgoCD won't delete PVCs automatically. Delete manually:

```bash
# Scale down the application first (optional, for safety)
kubectl scale statefulset ${RELEASE_NAME}-postgresql -n ${NAMESPACE} --replicas=0

# Delete the PVC
kubectl delete pvc data-${POSTGRES_POD} -n ${NAMESPACE}
```

---

## Step 4: Update Git Repository

Push the following changes to your Git repository:

### Chart.yaml
```yaml
dependencies:
  - name: postgres
    alias: postgresql
    repository: oci://registry-1.docker.io/cloudpirates
    version: 0.11.0
    condition: install.postgresql
```

### values.yaml (PostgreSQL section)
```yaml
postgresql:
  image:
    registry: docker.io
    repository: postgres
  persistence:
    enabled: true
    size: 10Gi
  auth:
    database: "bdrs"
    username: "bdrs"
    password: "password"  # Or use Vault reference
```

---

## Step 5: Sync ArgoCD Application

**Option A: Via ArgoCD UI**
1. Click "Sync" on your application
2. Enable "Prune" to remove old resources
3. Click "Synchronize"

**Option B: Via CLI**
```bash
argocd app sync <app-name> --prune
```

Wait for PostgreSQL pod to be ready:
```bash
kubectl wait --for=condition=ready pod/${POSTGRES_POD} -n ${NAMESPACE} --timeout=300s
```

---

## Step 6: Restore Data

```bash
cat ${BACKUP_FILE} | kubectl exec -i -n ${NAMESPACE} ${POSTGRES_POD} -- \
  bash -c "PGPASSWORD=${PG_PASSWORD} psql -U ${PG_USER}"
```

---

## Step 7: Verify & Re-enable Auto-Sync

```bash
# Verify data
kubectl exec -n ${NAMESPACE} ${POSTGRES_POD} -- bash -c \
  "PGPASSWORD=${PG_PASSWORD} psql -U ${PG_USER} -c '\l'"

# Re-enable auto-sync
argocd app set <app-name> --sync-policy automated
```

---

## Rollback

If issues occur:

1. Disable auto-sync
2. Revert Git changes (Chart.yaml, values.yaml)
3. Delete new PVC: `kubectl delete pvc data-${POSTGRES_POD} -n ${NAMESPACE}`
4. Sync ArgoCD application
5. Restore backup

---

## NOTICE

This work is licensed under the [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0).

- SPDX-License-Identifier: Apache-2.0
- SPDX-FileCopyrightText: 2024,2025,2026 Contributors to the Eclipse Foundation
- Source URL: https://github.com/eclipse-tractusx
