#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl rollout undo deployment/post-srv -n demo-k8s
