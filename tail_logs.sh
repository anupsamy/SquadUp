#!/bin/bash

# Name of your Docker container
CONTAINER_NAME="cpen321_app_M1"

# Tail the logs
docker logs -f "$CONTAINER_NAME"
