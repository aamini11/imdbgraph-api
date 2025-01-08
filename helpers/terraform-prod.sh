# https://stackoverflow.com/a/45971167
set -a
source ../.env
set +a

cd ../infra/terraform/live/prod || exit

terraform init
terraform apply