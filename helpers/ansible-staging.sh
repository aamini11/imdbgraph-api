# https://stackoverflow.com/a/45971167
set -a
source ../.env
set +a

cd ../infra/ansible || exit
ansible-playbook site.yml -i staging