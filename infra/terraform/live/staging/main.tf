module "app" {
  source = "../../shared"

  location              = "eastus2"
  resource_group_name   = "rg-imdbgraph-staging"
}