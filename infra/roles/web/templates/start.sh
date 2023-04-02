export POSTGRES_HOST="{{ lookup('env', 'POSTGRES_HOST') }}"
export POSTGRES_DB="{{ lookup('env', 'POSTGRES_DB') }}"
export POSTGRES_USER="{{ lookup('env', 'POSTGRES_USER') }}"
export POSTGRES_PASSWORD="{{ lookup('env', 'POSTGRES_PASSWORD') }}"

java -jar imdbgraph-0.0.1-SNAPSHOT.jar