#!/bin/sh

PROJECT=OurMemory
ARCHIVE=OurMemory.jar

cd $HOME/$PROJECT/package
cp -f $ARCHIVE $HOME/$PROJECT/deploy/
mv $ARCHIVE "$ARCHIVE"_"$(date +"%Y-%m-%d_%H-%M-%S")"
