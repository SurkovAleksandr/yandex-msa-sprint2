#!/bin/bash

set -e

echo "▶️ Checking booking-service deployment..."
kubectl get pods -l app=sprint2-task4 --namespace sprint2-task4

echo
echo "▶️ Checking service..."
kubectl get svc sprint2-task4 --namespace sprint2-task4 || echo "(No service found)"

echo
echo "▶️ Helm release:"
helm list | grep sprint2-task4 || echo "(No release found)"

echo
echo "▶️ Port-forward to test service locally:"
echo "  kubectl port-forward svc/booking-service 8080:80"
echo "  Then in another terminal:"
echo "    curl http://localhost:8080/ping"

echo
echo "▶️ Quick curl (if port-forward already running):"
curl --fail http://localhost:8080/ping && echo "✅ Reachable" || echo "❌ Not responding"
