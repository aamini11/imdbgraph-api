deployment "staging" {
  inputs = {
    name = "imdbgraph-api-staging"
    resource_group_name = "imdbgraph-api-staging-rg"
    location = "eastus"
  }
}

deployment "production" {
  inputs = {
    name = "imdbgraph-api"
    resource_group_name = "imdbgraph-rg"
    location = "eastus"
  }
}