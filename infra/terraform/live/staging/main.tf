module "azure_vm" {
  source = "../../modules/azure_vm"

  location            = "eastus"
  name                = "imdbgraph-api-staging"
  resource_group_name = "imdbgraph-staging-rg"
  public_key          = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAICsPIn35fd3+q19f826Ze/LclXsKwk+LVvrGJlQoua7j amini5454@gmail.com"
}