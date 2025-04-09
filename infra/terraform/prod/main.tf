module "app" {
  source = ".."

  location            = "eastus"
  name                = "imdbgraph-api"
  resource_group_name = "imdbgraph-api-rg"

  db_user     = var.db_user
  db_password = var.db_password
}