#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl apply -f manifests/rolling-update/post-srv-deployment.yaml
