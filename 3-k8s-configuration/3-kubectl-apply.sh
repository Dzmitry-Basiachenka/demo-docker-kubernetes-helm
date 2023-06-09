#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl apply -f manifests/namespace.yaml

kubectl apply -f manifests/user-db-service.yaml
kubectl apply -f manifests/user-db-cred.yaml
kubectl apply -f manifests/user-db-deployment.yaml

kubectl apply -f manifests/post-db-service.yaml
kubectl apply -f manifests/post-db-cred.yaml
kubectl apply -f manifests/post-db-deployment.yaml

kubectl apply -f manifests/user-srv-service.yaml
kubectl apply -f manifests/user-srv-cred.yaml
kubectl apply -f manifests/user-srv-deployment.yaml

kubectl apply -f manifests/post-srv-service.yaml
kubectl apply -f manifests/post-srv-cred.yaml
kubectl apply -f manifests/post-srv-deployment.yaml
