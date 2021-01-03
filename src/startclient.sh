#!/bin/bash
#creo il file listautenti se non esiste
if [[ ! -e worth/client/userList.txt ]]; then
    touch worth/client/userList.txt
fi

javac -cp .:./worth/gson-2.8.6.jar worth/*.java worth/client/*.java && java worth.client.ClientMain
