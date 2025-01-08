# https://stackoverflow.com/a/45971167
set -a
source ../.env
set +a

cd ../infra/terraform/live/staging || exit

terraform init
terraform apply
