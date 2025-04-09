resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_kubernetes_cluster" "default" {
  name                = "aks"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "imdbgraph-api"

  default_node_pool {
    name       = "agentpool"
    node_count = 2
    vm_size    = "Standard_DS2_v2"
  }

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_postgresql_flexible_server" "default" {
  name                = "db"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  version    = "16"
  sku_name   = "B_Standard_B1ms"
  storage_mb = 32768
  zone       = "1"

  public_network_access_enabled = false
  administrator_login           = var.db_user
  administrator_password        = var.db_password

  # prevent the possibility of accidental data loss
  lifecycle {
    prevent_destroy = true
  }
}