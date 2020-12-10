# #################################################################################################
# Makefile for setting up a dockerized database for testing purposes.
#
# Created by miquifant on 2020-11-09
# #################################################################################################
.PHONY: help

help:
	@clear
	@echo "  ___  ____  ____  _ ____  _  _     __   __    __    _  "
	@echo " / __)(  __)(_  _)(/(  __)( \/ )   / _\ (  )  (  )  / \ "
	@echo "( (_ \ ) _)   )(     ) _) / \/ \  /    \/ (_/\/ (_/\\\\\\_/ "
	@echo " \___/(____) (__)   (____)\_)(_/  \_/\_/\____/\____/(_) "
	@echo
	@echo "Creates and starts the getemall database: 'getemall-db'."
	@echo
	@echo "Usage: make [COMMAND]"
	@echo
	@echo "Commands:"
	@echo
	@echo "  - init       : Starts getemall-db properly configured onto local port 3306"
	@echo "  - destroy    : Completely removes the getemall-db instance"
	@echo "  - hard_reset : Completely removes the getemall-db instance and re-inits it"
	@echo
	@echo "  - start      : Starts the mariadb container onto local port 3306"
	@echo "  - schema     : Creates the 'getemall' schema"
	@echo
	@echo "  - test_data  : Populates the 'getemall' database with test data"
	@echo
	@echo "  - help       : Shows this message"
	@echo

init: start schema

confirm:
	@( read -p "This is a destructive operation. Are you sure? [y/N]: " sure && case "$$sure" in [yY]) true;; *) false;; esac )

destroy: confirm
	@echo "Destroying database..."
	@docker stop getemall-db
	@docker rm getemall-db

hard_reset: destroy init

start:
	@docker run -d --name getemall-db              \
	            -p 3306:3306                       \
	            -e MYSQL_ALLOW_EMPTY_PASSWORD=true \
	            mariadb:10
	@echo "Waiting for the database to start..."
	@sleep 10

schema:
	@echo "Creating getemall schema..."
	@docker cp sql/getemall_init.sql getemall-db:/
	@docker cp sql/getemall_privileges.sql getemall-db:/
	@docker cp sql/getemall_schema.sql getemall-db:/
	@docker exec getemall-db mariadb -uroot -e "source getemall_init.sql"
	@docker exec getemall-db rm /getemall_init.sql
	@docker exec getemall-db rm /getemall_privileges.sql
	@docker exec getemall-db rm /getemall_schema.sql

test_data:
	@echo "Populating getemall database with test data..."
	@docker cp sql/getemall_test_data.sql getemall-db:/tmp/test_data.sql
	@docker exec getemall-db mariadb -uroot -e "source /tmp/test_data.sql"
	@docker exec getemall-db rm /tmp/test_data.sql
