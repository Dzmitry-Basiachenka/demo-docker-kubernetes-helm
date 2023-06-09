#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl rollout history deployment/post-srv -n=demo-k8s
