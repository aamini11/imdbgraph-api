resource "azurerm_resource_group" "root" {
  name     = var.resource_group_name
  location = var.location
}

// ======================= Virtual Machine + SSH ===============================
resource "azurerm_linux_virtual_machine" "app" {
  resource_group_name   = azurerm_resource_group.root.name
  name                  = var.name
  admin_username        = var.name
  location              = var.location
  size                  = "Standard_B1s"

  network_interface_ids = [azurerm_network_interface.this.id]

  source_image_reference {
    offer     = "0001-com-ubuntu-server-focal"
    publisher = "canonical"
    sku       = "22_04-lts-gen2"
    version   = "latest"
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  admin_ssh_key {
    username   = var.name
    public_key = azurerm_ssh_public_key.this.public_key
  }
}

resource "azurerm_ssh_public_key" "this" {
  name                = "${var.name}-key"
  location            = var.location
  resource_group_name = azurerm_resource_group.root.name
  public_key          = var.public_key
}
// =============================================================================

// ============================= Networking ====================================
resource "azurerm_virtual_network" "this" {
  name                = "${var.name}-vnet"
  resource_group_name = azurerm_resource_group.root.name
  location            = var.location

  address_space = ["10.0.0.0/16"]
}

resource "azurerm_subnet" "this" {
  name                = "default"
  resource_group_name = azurerm_resource_group.root.name
  virtual_network_name = azurerm_virtual_network.this.name

  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_public_ip" "this" {
  name                = "${var.name}-public-ip"
  resource_group_name = azurerm_resource_group.root.name
  location            = var.location
  allocation_method = "Static"
}

resource "azurerm_network_interface" "this" {
  name                = "${var.name}-nic"
  resource_group_name = azurerm_resource_group.root.name
  location            = var.location

  ip_configuration {
    name                          = "ipconfig"
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.this.id
    subnet_id                     = azurerm_subnet.this.id
  }
}
// =============================================================================

// ============================== Firewall =====================================
resource "azurerm_network_interface_security_group_association" "this" {
  network_interface_id      = azurerm_network_interface.this.id
  network_security_group_id = azurerm_network_security_group.this.id
}

resource "azurerm_network_security_group" "this" {
  name                = "${var.name}-nsg"
  resource_group_name = azurerm_resource_group.root.name
  location            = var.location

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
// =============================================================================