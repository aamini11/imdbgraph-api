terraform {
  required_providers {
    azapi = {
      source  = "Azure/azapi"
      version = "~>2.2.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>4.14.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "tfstate"
    storage_account_name = "tfstateifgo5z83pc"
    container_name       = "tfstate"
    key                  = "terraform.tfstate"
  }
}

provider azurerm {
  features {}
  subscription_id = "331269d5-143f-4246-b389-9c1f41bb5882"
}