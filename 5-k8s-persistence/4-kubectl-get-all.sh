#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl get all -n=demo-k8s

kubectl get sc -n=demo-k8s
kubectl get pv -n=demo-k8s
kubectl get pvc -n=demo-k8s
