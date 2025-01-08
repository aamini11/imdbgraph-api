module "azure_vm" {
  source = "../../modules/azure_vm"
  location = "eastus"
  name = "imdbgraph-api-staging"
  resource_group_name = "imdbgraph-staging-rg2"
}