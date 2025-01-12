resource "azurerm_resource_group" "this" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_ssh_public_key" "this" {
  name                = "${var.name}-key"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name
  public_key          = var.public_key
}

################################################################################
# Application
################################################################################
resource "azurerm_kubernetes_cluster" "this" {
  name                = "imdbgraph-api-cluster"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  dns_prefix          = "imdbgraph-aks"

  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_B1s"
    os_sku     = "Ubuntu"
  }

  linux_profile {
    admin_username = "admin"
    ssh_key {
      key_data = var.public_key
    }
  }
}

################################################################################
# Database
################################################################################
resource "azurerm_postgresql_server" "this" {
  name                = "${var.name}-db"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  administrator_login          = var.name
  administrator_login_password = "H@Sh1CoR3!"
  sku_name           = "Standard_B1s"

  ssl_enforcement_enabled = false
  version                 = ""

  lifecycle {
    prevent_destroy = true
  }
}

################################################################################
# Networking
################################################################################
resource "azurerm_virtual_network" "this" {
  name                = "${var.name}-vnet"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  address_space = ["10.0.0.0/16"]
}

resource "azurerm_subnet" "this" {
  name                 = "default"
  resource_group_name  = azurerm_resource_group.this.name
  virtual_network_name = azurerm_virtual_network.this.name

  address_prefixes = ["10.0.1.0/24"]
}

resource "azurerm_public_ip" "public" {
  name                = "${var.name}-public-ip"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  allocation_method = "Static"
}

resource "azurerm_network_interface" "public" {
  name                = "${var.name}-nic"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  ip_configuration {
    name                          = "public-ipconfig"
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.public.id
    subnet_id                     = azurerm_subnet.this.id
  }
}

resource "azurerm_network_interface" "private" {
  name                = "${var.name}-db-nic"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  ip_configuration {
    name                          = "private-ipconfig"
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.this.id
  }
}

resource "azurerm_network_interface_security_group_association" "public" {
  network_interface_id      = azurerm_network_interface.public.id
  network_security_group_id = azurerm_network_security_group.public_firewall.id
}

resource "azurerm_network_interface_security_group_association" "private" {
  network_interface_id      = azurerm_network_interface.private.id
  network_security_group_id = azurerm_network_security_group.private_firewall.id
}

################################################################################
# Firewall
################################################################################
resource "azurerm_network_security_group" "public_firewall" {
  name                = "${var.name}-nsg"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

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

resource "azurerm_network_security_group" "private_firewall" {
  name                = "${var.name}-private-nsg"
  location            = var.location
  resource_group_name = azurerm_resource_group.this.name

  # Allow SSH only from the internal subnet 10.0.1.0/24
  security_rule {
    name                       = "AllowPrivateSubnetSSH"
    priority                   = 1001
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_address_prefix      = "10.0.1.0/24"
    source_port_range          = "*"
    destination_address_prefix = "*"
    destination_port_range     = "22"
  }
}