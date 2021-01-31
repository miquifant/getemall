alias minio-start='docker run -it --rm -p9000:9000 -v$PWD/minio:/data --name miqnio --user=$(id -u):$(id -g) minio/minio server /data'
mkdir minio
minio-start

#--------------- Otro terminal ---------------------#


## Situado en la carpeta donde se quiera guardar la .mc
#########################################################

docker create -it --name=mc --net=host -v$PWD/.mc:/root/.mc -v$PWD:/host --entrypoint=/bin/sh minio/mc
docker start mc
alias mc='docker exec mc mc'
#mc alias ls --json
mc alias set local http://localhost:9000 minioadmin minioadmin
mc alias rm gcs
mc alias rm s3
mc alias rm play


## Si se quiere gestionar desde máquina host:
## (Ejecutar cada vez que se modifique configuración)
#########################################################

sudo chown -R $(id -u):$(id -g) .mc
cat .mc/config.json


## Situado en la carpeta donde se encuentre la .mc
#########################################################

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
mc share upload --expire=1h --recursive local/getemall/_pub/avatars/
curl http://localhost:9000/getemall/                                               \
  -F x-amz-credential=minioadmin/20210130/us-east-1/s3/aws4_request                \
  -F x-amz-date=20210130T154045Z                                                   \
  -F x-amz-signature=23c325c4ffe8babe0082737858...6ed5c0c60a401b9a562f3807c562d1f8 \
  -F bucket=getemall                                                               \
  -F policy=eyJleHBpcmF0aW9uIjoiMjAyMS0wMS0zMFQ...LTEvczMvYXdzNF9yZXF1ZXN0Il1dfQ== \
  -F x-amz-algorithm=AWS4-HMAC-SHA256                                              \
  -F key=_pub/avatars/<NAME>                                                       \
  -F file=@<FILE>
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

$ mc admin user add local miqui password
$ mc admin user list local
$ mc admin user info local miqui
$ mc admin group add local admins miqui
$ mc admin group list local

mc mb -p --region=europe-spain local/getemall
mc cp host/.minioignore local/getemall/path/to/.minioignore
mc ls -r local
mc tree local

    local
    ├─ getemall
    │  └─ _pub
    │     └─ avatars
    └─ test1

mc stat local 

    Name      : getemall/
    Size      : 0 B    
    Type      : folder 
    Metadata  :
      Versioning: Un-versioned
      Location: us-east-1
      Policy: custom

    Name      : test1/
    Size      : 0 B    
    Type      : folder 
    Metadata  :
      Versioning: Un-versioned
      Location: us-east-1       <<<<<- ???
      Policy: none
















