#!/bin/bash
javac -cp .:./worth/gson-2.8.6.jar worth/*.java worth/server/*.java && java -cp .:./worth/gson-2.8.6.jar worth.server.ServerMain
