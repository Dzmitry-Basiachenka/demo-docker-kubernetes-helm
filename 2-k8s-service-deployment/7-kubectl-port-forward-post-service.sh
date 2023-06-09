#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl port-forward service/post-srv-service 8082:8080 -n demo-k8s
