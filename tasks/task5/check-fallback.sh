#!/bin/bash

set -e

echo "▶️ Testing fallback route..."
URL=$(minikube service booking-service --url)
echo $URL

curl -s http://$URL/ping #|| echo "Fallback route working"
