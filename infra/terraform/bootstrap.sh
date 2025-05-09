#!/bin/bash

################################################################################ 
# Bootstrap Terraform State Storage in Azure.
################################################################################

az group create --name tfstate --location eastus
# Storage Account
STORAGE_ACCOUNT_NAME="tfstate$(openssl rand -hex 8)"
az storage account create --resource-group tfstate --name "$STORAGE_ACCOUNT_NAME" --sku Standard_LRS --encryption-services blob
# Blob Containers
az storage container create --name "tfstate-staging" --account-name "$STORAGE_ACCOUNT_NAME"
az storage container create --name "tfstate-prod" --account-name "$STORAGE_ACCOUNT_NAME"

