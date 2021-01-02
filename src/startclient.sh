#!/bin/bash
javac -cp .:./worth/gson-2.8.6.jar worth/*.java worth/client/*.java && java worth.client.ClientMain
