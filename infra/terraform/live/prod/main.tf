module "azure_vm" {
  source = "../../modules/azure_vm"

  location            = "eastus"
  name                = "imdbgraph-api"
  resource_group_name = "imdbgraph-rg"
  public_key          = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAICESOYs4/kSLEyKzKPeKBou305mrqKSBZ5P3V4cuk47Z amini5454@gmail.com"
}