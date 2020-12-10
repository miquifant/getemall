```
  ___  ____  ____  _ ____  _  _     __   __    __    _
 / __)(  __)(_  _)(/(  __)( \/ )   / _\ (  )  (  )  / \
( (_ \ ) _)   )(     ) _) / \/ \  /    \/ (_/\/ (_/\\_/
 \___/(____) (__)   (____)\_)(_/  \_/\_/\____/\____/(_)
```
# Local Database
If you need to create a local database for testing you can use this script.

# Prerequisites
You need docker installed on your computer.

# Usage
```
$ make [COMMAND]

Commands:

  - init       : Starts getemall-db properly configured onto local port 3306
  - destroy    : Completely removes the getemall-db instance
  - hard_reset : Completely removes the getemall-db instance and re-inits it

  - start      : Starts the mariadb container onto local port 3306
  - schema     : Creates the 'getemall' schema

  - test_data  : Populates the 'getemall' database with test data

  - help       : Shows this message
```
