data "azurerm_client_config" "current" {}

resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

###############################################################################
# Kubernetes
###############################################################################
resource "azurerm_kubernetes_cluster" "this" {
  name                = "aks-imdbgraph"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "imdbgraph-api"

  default_node_pool {
    name       = "agentpool"
    node_count = 1
    vm_size    = "Standard_D2d_v5"
    temporary_name_for_rotation = "agentpool2"

    upgrade_settings {
      drain_timeout_in_minutes      = 0
      max_surge                     = "10%"
      node_soak_duration_in_minutes = 0
    }
  }

  network_profile {
    network_plugin      = "azure"
    network_plugin_mode = "overlay"
    network_data_plane  = "cilium"
    pod_cidr            = "192.168.0.0/16"
  }

  identity {
    type = "SystemAssigned"
  }

  oidc_issuer_enabled       = true
  workload_identity_enabled = true
}

###############################################################################
# Networking
###############################################################################
resource "azurerm_dns_zone" "imdbgraph" {
  name                = "api.staging.imdbgraph.org"
  resource_group_name = azurerm_resource_group.main.name
}

###############################################################################
# Security
###############################################################################
resource "azurerm_user_assigned_identity" "workload_id" {
  name                = "azure-alb-identity"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
}

resource "azurerm_federated_identity_credential" "this" {
  name                = azurerm_user_assigned_identity.workload_id.name
  resource_group_name = azurerm_user_assigned_identity.workload_id.resource_group_name
  parent_id           = azurerm_user_assigned_identity.workload_id.id
  audience            = ["api://AzureADTokenExchange"]
  issuer              = azurerm_kubernetes_cluster.this.oidc_issuer_url
  subject             = "system:serviceaccount:azure-alb-system:alb-controller-sa"
}

resource "azurerm_role_assignment" "reader" {
  role_definition_name = "Reader"
  scope                = azurerm_kubernetes_cluster.this.node_resource_group_id
  principal_id         = azurerm_user_assigned_identity.workload_id.principal_id
}

###############################################################################
# Database
###############################################################################
# resource "azurerm_postgresql_flexible_server" "default" {
#   name                = "db-imdbgraph"
#   location            = azurerm_resource_group.main.location
#   resource_group_name = azurerm_resource_group.main.name

#   version    = "16"
#   sku_name   = "B_Standard_B1ms"
#   storage_mb = 32768
#   zone       = "1"

#   public_network_access_enabled = false
#   administrator_login           = "postgres"
#   administrator_password        = random_password.db.result

#   # prevent the possibility of accidental data loss
#   lifecycle {
#     prevent_destroy = true
#   }
# }

# resource "random_password" "db" {
#   length           = 16
#   special          = true
# }