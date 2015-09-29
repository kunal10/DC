#!/bin/bash

javac -cp -sourcepath dc/BroadcastServer.java dc/Client.java dc/Message.java dc/Config.java
 
# Wait for 3 seconds for server to come up so that clients dont send requests before the server is up
echo "Waiting for server to come up"
sleep 3

echo "Starting Clients"
java -cp . dc.BroadcastServer "dc/broadcast_config" &
java -cp . dc.Client "dc/broadcast_config" "0" &
java -cp . dc.Client "dc/broadcast_config" "1" &
java -cp . dc.Client "dc/broadcast_config" "2" &
java -cp . dc.Client "dc/broadcast_config" "3" &
java -cp . dc.Client "dc/broadcast_config" "4" &

sleep 20
pkill -f Client
pkill -f Broadcast

