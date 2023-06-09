#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl logs deployment/user-db -n=demo-k8s
kubectl logs deployment/post-db -n=demo-k8s

kubectl logs deployment/user-srv -n=demo-k8s
kubectl logs deployment/post-srv -n=demo-k8s
