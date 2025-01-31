terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>4.16.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "tfstate"
    storage_account_name = "tfstateifgo5z83pc"
    container_name       = "tfstate-prod"
    key                  = "terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}