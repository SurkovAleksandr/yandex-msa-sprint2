#!/bin/bash

set -e

echo "▶️ Running in-cluster DNS test..."

kubectl run dns-test --rm -it \
  --image=busybox \
  --restart=Never \
  --namespace=sprint2-task4 \
  -- wget -qO- http://sprint2-task4/ping && echo "✅ Success" || echo "❌ Failed"