#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl get all -n=demo-k8s
