#!/bin/bash
#creo il file listautenti se non esiste
if [[ ! -e worth/client/userList.txt ]]; then
    touch worth/client/userList.txt
fi

#scarico il file gson.jar se non esistesse
wget -O worth/gson-2.8.6.jar -nc https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar

javac -cp .:./worth/gson-2.8.6.jar worth/*.java worth/client/*.java && java worth.client.ClientMain
