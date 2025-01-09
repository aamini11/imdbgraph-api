# How To Contribute

## Software Required

1. **Java**: Make sure you have JDK installed and configured. Should not be 
necessary if running from an IDE like vscode or IntelliJ that has Java
installed for you.

2. **Postgres**: For testing locally, make sure to install a local postgres 
database for easy debugging and testing when running the app locally.

3. **Docker**: For integration tests, the testing library TestContainers will 
spin up databases for use in tests. The library requires a docker runtime to
spin up those containers. This means having to install Docker Desktop for 
Windows/Mac.

## Optional Software to Install

1. **Azure CLI**: This project hosts all infra on Azure. Having the CLI to
debug or run admin tasks is useful for anyone working with the app's infra.
2. **Terraform**: Install Terraform CLI for debugging terraform commands locally.
3. **Ansible**: Install Ansible CLI for debugging ansible commands locally.

## Setup

1. Clone the repo: https://github.com/aamini11/imdbgraph-api
2. If this is the first time setting up the infrastructure, run the bootstrap
command to set up an azure storage account for hosting terraform tfstate files.