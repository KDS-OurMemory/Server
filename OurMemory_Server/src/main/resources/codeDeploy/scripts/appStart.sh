#!/bin/sh

PROJECT=OurMemory
ARCHIVE=OurMemory.jar

echo "> Check running $PROJECT pid."
CURRENT_PID=$(ps -ef | grep java | grep $ARCHIVE | awk '{print $2}') 
if [ -z $CURRENT_PID ]; then
 echo "> $PROJECT is not running."
else 
 echo "> kill -9 $CURRENT_PID"; kill -9 $CURRENT_PID; sleep 5
fi

echo "> Run $PROJECT"
cd $HOME/$PROJECT
nohup java -jar $HOME/$PROJECT/deploy/$ARCHIVE --spring.config.location=$HOME/$PROJECT/properties/application.yml >> $HOME/$PROJECT/logs/server.nohup 2>&1 &
