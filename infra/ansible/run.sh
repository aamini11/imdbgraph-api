# https://stackoverflow.com/a/45971167
set -a
source ../../.env
set +a

ansible-playbook -i staging main.yml