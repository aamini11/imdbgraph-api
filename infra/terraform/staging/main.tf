module "app" {
  source = ".."

  location            = "eastus"
  name                = "imdbgraph-staging-api"
  resource_group_name = "imdbgraph-staging-api-rg"

  db_user     = var.db_user
  db_password = var.db_password
}