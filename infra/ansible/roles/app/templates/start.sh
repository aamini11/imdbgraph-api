#!/bin/bash

export DATABASE_HOST="{{ db_host }}"
export DATABASE_DB="{{ db_name }}"
export DATABASE_USER="{{ db_user }}"
export DATABASE_PASSWORD="{{ db_password }}"

export OMDB_KEY="{{ omdb_key }}"

cd "/home/{{ ansible_user }}" || exit
java -jar imdbgraph-api-0.0.1-SNAPSHOT.jar