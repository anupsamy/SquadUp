#!/bin/bash

# Configurable variables
CONTAINER_NAME="mongo_instance"
DB_NAME="SquadUp"

echo "Listing all groups in database '$DB_NAME'..."

docker exec -i "$CONTAINER_NAME" mongosh --quiet --eval '
use("'$DB_NAME'");
db.groups.find().forEach(u => printjson(u));
'
