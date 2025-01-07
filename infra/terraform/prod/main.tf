module "azure_vm" {
  source = "../modules/azure_vm"
  location = "eastus"
  name = "imdbgraph-api"
  resource_group_name = "imdbgraph-rg"
}