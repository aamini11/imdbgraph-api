#!/bin/bash

set -a
source ./secrets.env
set +a

LOCATION=eastus

# Set up credentials
az ad sp create-for-rbac --role="Contributor" --scopes="/subscriptions/$ARM_SUBSCRIPTION_ID"
az ad app federated-credential create --id "$ARM_CLIENT_ID" --parameters credentials.json

# Set up storage account for storing terraform state files.
RANDOM=$(openssl rand -hex 8)
STORAGE_ACCOUNT_NAME=tfstate$RANDOM
RESOURCE_GROUP_NAME=tfstate
CONTAINER_NAME=tfstate

# Create resource group
az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION"

# Create storage account
az storage account create --resource-group "$RESOURCE_GROUP_NAME" --name "$STORAGE_ACCOUNT_NAME" --sku Standard_LRS --encryption-services blob

# Create blob container
az storage container create --name "$CONTAINER_NAME" --account-name "$STORAGE_ACCOUNT_NAME"
