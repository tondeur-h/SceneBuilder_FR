#!/bin/bash
cd ~/dev/HT-scenebuilder
gradle assemble
sudo mv /home/herve/dev/HT-scenebuilder/build/libs/scenebuilder-all-8.2.0-FRENCH-all.jar /opt/SceneBuilder/app/dist.jar

pftp -n -v < deploy.ftp

/opt/SceneBuilder/SceneBuilder &
