#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl get all -n=demo-k8s
kubectl get ing -n=demo-k8s
kubectl describe ing ingress-srv -n=demo-k8s
