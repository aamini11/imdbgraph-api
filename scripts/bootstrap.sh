#!/bin/bash

# REPLACE WITH YOUR SUB ID!!!!
export ARM_SUBSCRIPTION_ID="11111111-1111-1111-1111-111111111111"

# Set up credentials
az ad sp create-for-rbac --role="Contributor" --scopes="/subscriptions/$ARM_SUBSCRIPTION_ID"

# Copy the results returned from this command into the following env vars:
export ARM_CLIENT_ID=""
export ARM_CLIENT_SECRET=""
export ARM_TENANT_ID=""

# Create resource group
az group create --name "$RESOURCE_GROUP_NAME" --location eastus

# Create Storage Account.
STORAGE_ACCOUNT_NAME="tfstate$(openssl rand -hex 8)"
RESOURCE_GROUP_NAME="tfstate"
az storage account create --resource-group "$RESOURCE_GROUP_NAME" --name "$STORAGE_ACCOUNT_NAME" --sku Standard_LRS --encryption-services blob
# Create Blob Containers
az storage container create --name "tfstate-staging" --account-name "$STORAGE_ACCOUNT_NAME"
az storage container create --name "tfstate-prod" --account-name "$STORAGE_ACCOUNT_NAME"

# Bootstrap FluxCD
az aks get-credentials --resource-group rg-imdbgraph-staging --name aks-imdbgraph
export GITHUB_TOKEN=""
flux bootstrap github \
  --token-auth \
  --owner=aamini11 \
  --repository=https://github.com/aamini11/imdbgraph-api \
  --branch=main \
  --path=infra/clusters/quick-deploy.yaml \
  --personal