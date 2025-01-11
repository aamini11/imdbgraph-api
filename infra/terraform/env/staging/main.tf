module "app" {
  source = "../../app"

  location            = "eastus"
  name                = "imdbgraph-staging-api"
  resource_group_name = "imdbgraph-staging-api-rg"
  # Public key (NOT SECRET)
  public_key = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAICsPIn35fd3+q19f826Ze/LclXsKwk+LVvrGJlQoua7j amini5454@gmail.com"
}