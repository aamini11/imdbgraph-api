component "server" {
  source = "./server"

  inputs = {
    name = var.name
    resource_group_name = var.resource_group_name
    location = var.location
  }

  providers = {
    azapi   = provider.azapi.this
    azurerm = provider.azurerm.this
  }
}