#!/bin/bash

# Configurable variables
CONTAINER_NAME="mongo_instance"
DB_NAME="SquadUp"

echo "Deleting all Groups in database '$DB_NAME'..."

docker exec -i "$CONTAINER_NAME" mongosh --quiet --eval '
use("'$DB_NAME'");
db.groups.deleteMany({});
'
