data "azurerm_client_config" "current" {}

resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

################################################################################
# Virtual Machine
################################################################################
resource "azurerm_ssh_public_key" "default" {
  name                = "${var.name}-key"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  public_key          = var.public_key
}

resource "azurerm_linux_virtual_machine" "default" {
  name                = "${var.name}-vm"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  size                  = "Standard_B1ms"
  admin_username        = var.name
  network_interface_ids = [azurerm_network_interface.public.id]

  identity {
    type = "SystemAssigned"
  }

  source_image_reference {
    offer     = "0001-com-ubuntu-server-focal"
    publisher = "canonical"
    sku       = "20_04-lts-gen2"
    version   = "latest"
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  admin_ssh_key {
    username   = var.name
    public_key = azurerm_ssh_public_key.default.public_key
  }
}

################################################################################
# Database
################################################################################
resource "azurerm_postgresql_flexible_server" "default" {
  name                = "imdbgraph-db"
  location            = "eastus2"
  resource_group_name = azurerm_resource_group.main.name

  version    = "16"
  sku_name   = "B_Standard_B1ms"
  storage_mb = 32768
  zone       = "1"

  public_network_access_enabled = false
  administrator_login           = "postgres"
  administrator_password        = azurerm_key_vault_secret.db_password.value

  # prevent the possibility of accidental data loss
  lifecycle {
    prevent_destroy = true
  }
}

################################################################################
# Key Vault
################################################################################
resource "random_id" "keyvault" {
  byte_length = 8
}

resource "random_password" "db" {
  length = 20
}

resource "azurerm_key_vault" "default" {
  name                = "kv-${random_id.keyvault.hex}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"

  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id

    secret_permissions = [
      "Set", "Get", "List", "Delete", "Recover"
    ]
  }
}

resource "azurerm_key_vault_secret" "db_password" {
  name         = "db-admin-password"
  value        = random_password.db.result
  key_vault_id = azurerm_key_vault.default.id
}

resource "azurerm_key_vault_access_policy" "app_vault_access" {
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_linux_virtual_machine.default.identity[0].principal_id
  key_vault_id = azurerm_key_vault.default.id

  secret_permissions = [
    "Get", "List"
  ]
}

################################################################################
# Networking
################################################################################
resource "azurerm_virtual_network" "default" {
  name                = "${var.name}-vnet"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  address_space = ["10.0.0.0/16"]
}

resource "azurerm_subnet" "default" {
  name                 = "default"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.default.name

  address_prefixes = ["10.0.1.0/24"]
}

resource "azurerm_public_ip" "default" {
  name                = "${var.name}-public-ip"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  allocation_method = "Static"
}

resource "azurerm_network_interface" "public" {
  name                = "${var.name}-nic"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  ip_configuration {
    name                          = "public-ipconfig"
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.default.id
    subnet_id                     = azurerm_subnet.default.id
  }
}

resource "azurerm_network_interface_security_group_association" "public" {
  network_interface_id      = azurerm_network_interface.public.id
  network_security_group_id = azurerm_network_security_group.firewall.id
}

################################################################################
# Firewall
################################################################################
resource "azurerm_network_security_group" "firewall" {
  name                = "${var.name}-nsg"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  security_rule {
    access                     = "Allow"
    destination_address_prefix = "*"
    destination_port_range     = "22"
    direction                  = "Inbound"
    name                       = "AllowAnySSHInbound"
    priority                   = 1001
    protocol                   = "Tcp"
    source_address_prefix      = "*"
    source_port_range          = "*"
  }

  security_rule {
    access                     = "Allow"
    destination_address_prefix = "*"
    destination_port_range     = "80"
    direction                  = "Inbound"
    name                       = "AllowAnyHTTPInbound"
    priority                   = 320
    protocol                   = "Tcp"
    source_address_prefix      = "*"
    source_port_range          = "*"
  }

  security_rule {
    access                     = "Allow"
    destination_address_prefix = "*"
    destination_port_range     = "443"
    direction                  = "Inbound"
    name                       = "AllowAnyHTTPSInbound"
    priority                   = 310
    protocol                   = "Tcp"
    source_address_prefix      = "*"
    source_port_range          = "*"
  }
}