#!/usr/bin/env bash
set -euo pipefail

mkdir -p /var/app/secrets

aws secretsmanager get-secret-value \
  --secret-id "$FIREBASE_SECRET_ID" \
  --query SecretString \
  --output text \
  --region "$AWS_REGION" \
  > "$FIREBASE_CREDENTIALS_PATH"

chmod 600 "$FIREBASE_CREDENTIALS_PATH"
