# #################################################################################################
# Makefile for setting up a dockerized database, and a block storage for testing purposes.
#
# Created by miquifant on 2020-11-09
# #################################################################################################
.PHONY: help

SHELL := /usr/bin/env bash

help:
	@clear
	@echo "  ___  ____  ____  _ ____  _  _     __   __    __    _  "
	@echo " / __)(  __)(_  _)(/(  __)( \/ )   / _\ (  )  (  )  / \ "
	@echo "( (_ \ ) _)   )(     ) _) / \/ \  /    \/ (_/\/ (_/\\\\_/ "
	@echo " \___/(____) (__)   (____)\_)(_/  \_/\_/\____/\____/(_) "
	@echo
	@echo "Creates and starts the getemall database 'getemall-db' and"
	@echo "the getemall bucket in an object storage server 'minio-getemall'"
	@echo
	@echo "Usage: make [COMMAND]"
	@echo
	@echo "Commands:"
	@echo
	@echo "  - init       : Starts database and object storage servers in containers"
	@echo "  - destroy    : Completely removes the db, os and cli instances"
	@echo "  - reset      : Completely removes the db, os and cli instances and re-inits them"
	@echo "  - test-data  : Populates the 'getemall' db and object storage with test data"
	@echo
	@echo "  - init-db    : Starts getemall-db properly configured onto local port 3306"
	@echo "  - destroy-db : Completely removes the getemall-db instance"
	@echo "  - reset-db   : Completely removes the getemall-db instance and re-inits it"
	@echo "  - start-db   : Starts the mariadb container onto local port 3306"
	@echo "  - schema-db  : Creates the 'getemall' schema"
	@echo "  - test-data-db : Populates the 'getemall' db with test data"
	@echo
	@echo "  - init-os    : Starts a getemall os properly configured onto local port 9000"
	@echo "                 It also creates a client (mc)"
	@echo "  - destroy-os : Completely removes the getemall os instance (and client)"
	@echo "  - reset-os   : Completely removes the getemall os instance and re-inits it"
	@echo "  - start-os   : Starts the minio container onto local port 9000"
	@echo "  - schema-os  : Creates the 'getemall' bucket and its structure"
	@echo "  - test-data-os : Populates the 'getemall' bucket with test data"
	@echo
	@echo "  - help       : Shows this message"
	@echo

define banner
	[[ ! -z $1 ]] && echo -e "\n> $1\n================================================================================"
endef

confirm:
	@( read -p "This is a destructive operation. Are you sure? [y/N]: " sure && case "$$sure" in [yY]) true;; *) false;; esac )

init: init-db init-os
destroy: destroy-db destroy-os
reset: destroy init
test-data: test-data-db test-data-os

init-db: start-db schema-db
reset-db: destroy-db init-db

destroy-db: confirm
	@$(banner,"Destroying database...")
	@docker stop getemall-db
	@docker rm getemall-db

start-db:
	@$(call banner,"Starting db...")
	@docker run -d --name getemall-db              \
	            -p 3306:3306                       \
	            -e MYSQL_ALLOW_EMPTY_PASSWORD=true \
	            mariadb:10
	@echo "Waiting for the database to start..."
	@sleep 10

schema-db:
	@$(call banner,"Creating getemall schema...")
	@docker cp sql/getemall_init.sql getemall-db:/
	@docker cp sql/getemall_privileges.sql getemall-db:/
	@docker cp sql/getemall_schema.sql getemall-db:/
	@docker exec getemall-db mariadb -uroot -e "source getemall_init.sql"
	@docker exec getemall-db rm /getemall_init.sql
	@docker exec getemall-db rm /getemall_privileges.sql
	@docker exec getemall-db rm /getemall_schema.sql

test-data-db:
	@$(call banner,"Populating getemall database with test data...")
	@docker cp sql/getemall_test_data.sql getemall-db:/tmp/test_data.sql
	@docker exec getemall-db mariadb -uroot -e "source /tmp/test_data.sql"
	@docker exec getemall-db rm /tmp/test_data.sql

init-os: start-os schema-os info-os
reset-os: destroy-os init-os

destroy-os: confirm
	@$(call banner,"Removing minio and mc...")
	@docker stop minio-getemall > /dev/null
	@docker rm minio-getemall > /dev/null
	@rm -fr minio-data
	@echo -e "Removed minio"
	@docker exec mc-getemall rm -fr /root/.mc 
	@docker stop mc-getemall > /dev/null
	@docker rm mc-getemall > /dev/null
	@echo sudo rm -fr .mc
	@echo -e "Removed mc"

start-os: start-mc start-minio

start-mc:
	@$(call banner,"Creating mc... (TODO as root)") # TODO
	@docker create -it --name=mc-getemall --net=host -v${PWD}/.mc:/root/.mc -v${PWD}/objects:/data --entrypoint=/bin/sh minio/mc
	@docker start mc-getemall > /dev/null

start-minio:
	@$(call banner,"Creating minio...")
	@mkdir -p minio-data
	@docker create -p9000:9000 -v${PWD}/minio-data:/data --name minio-getemall --user=$(shell id -u):$(shell id -g) minio/minio minio server /data
	@docker start minio-getemall > /dev/null
	@sleep 1

schema-os:
	@$(call banner,"Initializing alias...") # set local and remove gcs, s3 and play
	@docker exec mc-getemall mc alias rm gcs
	@docker exec mc-getemall mc alias rm local
	@docker exec mc-getemall mc alias rm play
	@docker exec mc-getemall mc alias rm s3
	@docker exec mc-getemall mc alias set local http://localhost:9000 minioadmin minioadmin
	@$(call banner,"Configuring users and groups...")
	@docker exec mc-getemall mc admin user add local miqui password
	@docker exec mc-getemall mc admin group add local admins miqui
	@$(call banner,"Creating buckets...")
	@docker exec mc-getemall mc mb -p --region=europe-spain local/getemall

test-data-os:
	@$(call banner,"Populating getemall bucket with test files...")
	@docker exec mc-getemall mc cp -r data/avatars local/getemall/_pub/
	@echo -e "\nCreated buckets. Tree:\n----------------------"
	@docker exec mc-getemall mc tree local
	@echo -e "\nUploaded files:\n---------------"
	@docker exec mc-getemall mc ls -r local

info-os:
	@$(call banner,"Exiting...")
	@echo -e "Finished starting minio."
	@echo -e "\n- You can define alias mc='docker exec mc-getemall mc' to execute mc commands"
	@echo -en "- You can access minio using a browser: Browser Access for "
	@docker exec mc-getemall mc alias list
