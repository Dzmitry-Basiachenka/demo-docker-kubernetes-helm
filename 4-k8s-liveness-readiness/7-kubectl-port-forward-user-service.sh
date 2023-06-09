#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl port-forward service/user-srv-service 8081:8080 -n demo-k8s
