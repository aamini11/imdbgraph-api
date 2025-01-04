required_providers {
  azapi = {
    source  = "Azure/azapi"
    version = "1.13.1"
  }
  azurerm = {
    source  = "hashicorp/azurerm"
    version = "3.108.0"
  }
}

provider "azapi" "this" {}

provider "azurerm" "this" {
  features {}
}