# Launching application from the IDE

## Setup and start local Database using docker

Move to `${GETEMALL_HOME}/database` directory and follow [these instructions](../../../../database/README.md).

## Run/Debug configuration

You can configure your IDE to launch application using:

- Main class: `miquifant.getemall.Main`
- VM options: `-Dlogback.configurationFile=src/test/resources/conf/logback.xml`
- Program arguments: `serve`
- Environment variables: `GETEMALL_CONF=src/test/resources/conf`
