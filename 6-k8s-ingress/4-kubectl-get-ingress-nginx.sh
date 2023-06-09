#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl get pods -n ingress-nginx
