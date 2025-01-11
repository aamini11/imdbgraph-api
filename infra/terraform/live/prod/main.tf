module "app" {
  source = "../../modules/app"

  location            = "eastus"
  name                = "imdbgraph-api"
  resource_group_name = "imdbgraph-api-rg"
  # Public key (NOT SECRET)
  public_key          = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAICESOYs4/kSLEyKzKPeKBou305mrqKSBZ5P3V4cuk47Z amini5454@gmail.com"
}