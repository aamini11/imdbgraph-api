# IMDb Infrastructure

This folder contains all the code for setting up IMDb's infrastructure.
(i.e. all the servers that the app will be run on). This code makes sure all
the right servers are set up and configured using 2 automation tools:

1. **Terraform**: Makes sure servers are provisioned. Meaning that all the
servers are set up and running on whatever cloud provider is hosting them
(Azure in our case).
2. **Ansible**: Makes sure servers are configured. Meaning that all the
right software is installed and the right systems settings are enabled.