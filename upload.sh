#!/bin/bash
cd ~/Programmering/sdmapeg/project
mvn package
cd server/target
mv server-1.0-SNAPSHOT.jar server.jar
scp server.jar Trivoc@95.80.13.184:./sdmapeg/
