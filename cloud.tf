terraform {
  // Also requires manually setting up credentials for Azure:
  // https://developer.hashicorp.com/terraform/tutorials/azure-get-started/azure-remote#update-the-hcp-terraform-environment-variables
  cloud {
    organization = "imdbgraph"
    hostname = "app.terraform.io" # Optional; defaults to app.terraform.io

    workspaces {
      name = "imdbgraph-api"
      project = "imdbgraph-api"
    }
  }
}