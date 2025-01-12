#!/bin/bash

LOCATION=eastus

# REPLACE WITH YOUR SUB ID!
ARM_SUBSCRIPTION_ID="YOUR_ID"

# Set up credentials
az ad sp create-for-rbac --role="Contributor" --scopes="/subscriptions/$ARM_SUBSCRIPTION_ID"
# Copy the results returned from this command into the following env vars:
# ARM_CLIENT_ID
# ARM_CLIENT_SECRET
# ARM_TENANT_ID

# Set up storage account for storing terraform state files.
RANDOM=$(openssl rand -hex 8)
STORAGE_ACCOUNT_NAME="tfstate$RANDOM"
RESOURCE_GROUP_NAME="tfstate"

# Create resource group
az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION"

# Create storage account
az storage account create --resource-group "$RESOURCE_GROUP_NAME" --name "$STORAGE_ACCOUNT_NAME" --sku Standard_LRS --encryption-services blob

# Create blob container
az storage container create --name "tfstate-staging" --account-name "$STORAGE_ACCOUNT_NAME"
az storage container create --name "tfstate-prod" --account-name "$STORAGE_ACCOUNT_NAME"