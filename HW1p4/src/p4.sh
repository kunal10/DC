#!/bin/bash

javac -cp -sourcepath dc/UnicastServer.java dc/Client.java dc/Message.java dc/Config.java

# Wait for the server to come up so that clients dont try to establish a connection before that.
sleep 3

java -cp . dc.UnicastServer "dc/unicast_config" &
java -cp . dc.Client "dc/unicast_config" "0" &
java -cp . dc.Client "dc/unicast_config" "1" &
java -cp . dc.Client "dc/unicast_config" "2" &
java -cp . dc.Client "dc/unicast_config" "3" &
java -cp . dc.Client "dc/unicast_config" "4" &

sleep 20
pkill -f Client
pkill -f Unicast
