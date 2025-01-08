cd ../infra/terraform/live/prod || exit
terraform fmt

cd ../staging || exit
terraform fmt

cd ../../modules/azure_vm || exit
terraform fmt