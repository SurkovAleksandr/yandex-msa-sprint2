#!/bin/bash

set -e

kubectl apply -f ./templates/deployment-ui.yaml
kubectl apply -f ./templates/service-ui.yaml
