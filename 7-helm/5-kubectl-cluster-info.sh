#!/bin/sh

alias kubectl="minikube kubectl --"

kubectl cluster-info
kubectl cluster-info dump
