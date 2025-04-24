module "app" {
  source = "../../app"

  location            = "eastus2"
  resource_group_name = "rg-imdbgraph-staging"
}