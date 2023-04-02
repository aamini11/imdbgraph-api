export DATABASE_HOST="{{ lookup('env', 'DATABASE_HOST') }}"
export DATABASE_DB="{{ lookup('env', 'DATABASE_DB') }}"
export DATABASE_USER="{{ lookup('env', 'DATABASE_USER') }}"
export DATABASE_PASSWORD="{{ lookup('env', 'DATABASE_PASSWORD') }}"

java -jar imdbgraph-0.0.1-SNAPSHOT.jar