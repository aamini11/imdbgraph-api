required_providers {
  azapi = {
    source  = "Azure/azapi"
    version = "2.2.0"
  }
  azurerm = {
    source  = "hashicorp/azurerm"
    version = "4.14.0"
  }
}

provider "azapi" "this" {}

provider "azurerm" "this" {
  config {
    features {}
  }
}