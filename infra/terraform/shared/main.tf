data "azurerm_client_config" "current" {}

resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_kubernetes_cluster" "this" {
  name                = "aks-imdbgraph"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "imdbgraph-api"

  default_node_pool {
    name       = "agentpool"
    node_count = 2
    vm_size    = "Standard_DS2_v2"

    upgrade_settings {
      drain_timeout_in_minutes      = 0
      max_surge                     = "10%"
      node_soak_duration_in_minutes = 0
    }
  }

  identity {
    type = "SystemAssigned"
  }

  oidc_issuer_enabled       = true
  workload_identity_enabled = true
}

resource "azurerm_user_assigned_identity" "workload_id" {
  name                = "workload-id"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
}

resource "azurerm_federated_identity_credential" "this" {
  name                = azurerm_user_assigned_identity.workload_id.name
  resource_group_name = azurerm_user_assigned_identity.workload_id.resource_group_name
  parent_id           = azurerm_user_assigned_identity.workload_id.id
  audience            = ["api://AzureADTokenExchange"]
  issuer              = azurerm_kubernetes_cluster.this.oidc_issuer_url
  subject             = "system:serviceaccount:imdbgraph:workload-identity-sa"
}

resource "azurerm_role_assignment" "key_vault_user" {
  role_definition_name = "Key Vault Secrets User"
  scope                = azurerm_key_vault.this.id
  principal_id         = azurerm_user_assigned_identity.workload_id.principal_id
}

resource "azurerm_postgresql_flexible_server" "default" {
  name                = "db-imdbgraph"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  version    = "16"
  sku_name   = "B_Standard_B1ms"
  storage_mb = 32768
  zone       = "1"

  public_network_access_enabled = false
  administrator_login           = "postgres"
  administrator_password        = random_password.db.result

  # prevent the possibility of accidental data loss
  lifecycle {
    prevent_destroy = true
  }
}

resource "azurerm_key_vault" "this" {
  name                        = "kv-imdbgraph"
  location                    = azurerm_resource_group.main.location
  resource_group_name         = azurerm_resource_group.main.name

  tenant_id                   = data.azurerm_client_config.current.tenant_id

  sku_name = "standard"
  enabled_for_disk_encryption = true
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false

  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id

    key_permissions = [
      "Get",
    ]

    secret_permissions = [
      "Get",
    ]

    storage_permissions = [
      "Get",
    ]
  }
}

resource "random_password" "db" {
  length           = 16
  special          = true
}