terraform {
  required_providers {
    azapi = {
      source  = "Azure/azapi"
      version = "2.2.0"
    }
  }
}

resource "azurerm_resource_group" "rg" {
  name     = var.resource_group_name
  location = var.location
}

// ======================= Virtual Machine + SSH ===============================
resource "azurerm_linux_virtual_machine" "vm" {
  name                  = var.name
  admin_username        = var.name
  location              = var.location
  resource_group_name   = var.resource_group_name
  network_interface_ids = [azurerm_network_interface.nic.id]
  size                  = "Standard_B2s"

  source_image_reference {
    offer     = "0001-com-ubuntu-server-focal"
    publisher = "canonical"
    sku       = "20_04-lts-gen2"
    version   = "latest"
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Premium_LRS"
  }

  admin_ssh_key {
    username   = var.name
    public_key = azapi_resource_action.ssh_public_key_gen.output.publicKey
  }

  boot_diagnostics {
  }
}

resource "azapi_resource" "ssh_public_key" {
  type      = "Microsoft.Compute/sshPublicKeys@2022-11-01"
  name      = "${var.name}-key"
  location  = var.location
  parent_id = azurerm_resource_group.rg.id
}

resource "azapi_resource_action" "ssh_public_key_gen" {
  type        = "Microsoft.Compute/sshPublicKeys@2022-11-01"
  resource_id = azapi_resource.ssh_public_key.id
  action      = "generateKeyPair"
  method      = "POST"

  response_export_values = ["publicKey", "privateKey"]
}
// =============================================================================

// ============================= Networking ====================================
resource "azurerm_virtual_network" "virtual_network" {
  name                = "${var.name}-vnet"
  resource_group_name = var.resource_group_name
  location            = var.location

  address_space = ["10.0.0.0/16"]
}

resource "azurerm_network_interface" "nic" {
  name                = "${var.name}-nic"
  resource_group_name = var.resource_group_name
  location            = var.location

  ip_configuration {
    name                          = "ipconfig1"
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.public_ip.id
    subnet_id                     = azurerm_subnet.subnet.id
  }
}

resource "azurerm_subnet" "subnet" {
  name                = "default"
  resource_group_name = var.resource_group_name

  virtual_network_name = azurerm_virtual_network.virtual_network.name
  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_public_ip" "public_ip" {
  name                = "${var.name}-ip"
  resource_group_name = var.resource_group_name
  location            = var.location

  allocation_method = "Static"
}
// =============================================================================

// ============================== Firewall =====================================
resource "azurerm_network_interface_security_group_association" "nic_sg" {
  network_interface_id      = azurerm_network_interface.nic.id
  network_security_group_id = azurerm_network_security_group.nsg.id
}

resource "azurerm_network_security_group" "nsg" {
  name                = "${var.name}-nsg"
  resource_group_name = var.resource_group_name
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
    access                      = "Allow"
    destination_address_prefix  = "*"
    destination_port_range      = "80"
    direction                   = "Inbound"
    name                        = "AllowAnyHTTPInbound"
    priority                    = 320
    protocol                    = "Tcp"
    source_address_prefix       = "*"
    source_port_range           = "*"
  }

  security_rule {
    access                      = "Allow"
    destination_address_prefix  = "*"
    destination_port_range      = "443"
    direction                   = "Inbound"
    name                        = "AllowAnyHTTPSInbound"
    priority                    = 310
    protocol                    = "Tcp"
    source_address_prefix       = "*"
    source_port_range           = "*"
  }
}
// =============================================================================