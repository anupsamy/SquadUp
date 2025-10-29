.PHONY: build up down dev

# Build the Docker containers
build:
	cd backend && docker-compose --env-file .env build

# Bring up containers in detached mode
up:
	cd backend && docker-compose --env-file .env up -d

# Stop and remove containers
down:
	cd backend && docker-compose --env-file .env down

# Build and start in one step
dev: build up

rebuild: down dev