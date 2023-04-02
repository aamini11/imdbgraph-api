#!/bin/bash

export DATABASE_HOST="{{ db_host }}"
export DATABASE_DB="{{ db_name }}"
export DATABASE_USER="{{ db_user }}"
export DATABASE_PASSWORD="{{ db_password }}"

cd "home/{{ ansible_user }}" || return
java -jar imdbgraph-0.0.1-SNAPSHOT.jar