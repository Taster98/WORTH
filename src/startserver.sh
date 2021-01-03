#!/bin/bash
#Creo il dbutenti se non esiste
if [[ ! -e worth/server/Database/userDb.json ]]; then
    touch worth/server/Database/userDb.json
fi

#Creo le tre liste se non esistono
if [[ ! -e worth/server/Database/progetti/auxProjectList.json ]]; then
    touch worth/server/Database/progetti/auxProjectList.json
fi

if [[ ! -e worth/server/Database/progetti/listaIpAddress.json ]]; then
    touch worth/server/Database/progetti/listaIpAddress.json
fi

if [[ ! -e worth/server/Database/progetti/listaProgetti.json ]]; then
    touch worth/server/Database/progetti/listaProgetti.json
fi

#scarico il file gson.json se non esistesse
wget -O worth/gson-2.8.6.jar -nc https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar

javac -cp .:./worth/gson-2.8.6.jar worth/*.java worth/server/*.java && java -cp .:./worth/gson-2.8.6.jar worth.server.ServerMain
