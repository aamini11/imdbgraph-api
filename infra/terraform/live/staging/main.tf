module "azure_vm" {
  source = "../../modules/azure_vm"

  location            = "eastus"
  name                = "imdbgraph-api-staging"
  resource_group_name = "imdbgraph-staging-rg"
  public_key          = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJ9praEryUgwMl7JMC7HED4nx9cF/PflO44/d6IChh0P amini5454@gmail.com"
}