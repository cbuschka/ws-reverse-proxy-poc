PROJECT_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

start:	build
	cd $(PROJECT_DIR) && \
	docker-compose up

build:
	cd $(PROJECT_DIR) && \
	mvn package
