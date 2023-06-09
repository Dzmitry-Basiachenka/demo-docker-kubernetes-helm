#!/bin/sh

helm upgrade --set namespace=demo-k8s-helm demo-helm ./demo-helm
