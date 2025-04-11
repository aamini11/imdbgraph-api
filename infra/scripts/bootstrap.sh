#!/bin/bash

# Create Resource Group
az group create --name tfstate --location eastus
# Create Storage Account
STORAGE_ACCOUNT_NAME="tfstate$(openssl rand -hex 8)"
az storage account create --resource-group tfstate --name "$STORAGE_ACCOUNT_NAME" --sku Standard_LRS --encryption-services blob
# Create Blob Containers
az storage container create --name "tfstate-staging" --account-name "$STORAGE_ACCOUNT_NAME"
az storage container create --name "tfstate-prod" --account-name "$STORAGE_ACCOUNT_NAME"

# Allow login for kubectl
az aks get-credentials --resource-group rg-imdbgraph-staging --name aks-imdbgraph
# Setup
kubectl create namespace imdbgraph
# ArgoCD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
# External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets \
   external-secrets/external-secrets \
    -n external-secrets \
    --create-namespace