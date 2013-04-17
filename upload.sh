#!/bin/bash
cd ~/Programmering/sdmapeg/project
mvn package
cd server/target
scp server-1.0-SNAPSHOT.jar Trivoc@95.80.13.184:./sdmapeg/server.jar
