#!/usr/bin/env bash

if [ -z $1 ];then
	echo 'Please input version'
	exit -1
fi

./gradlew war

if [ ! -d ../war/$1 ];then
    mkdir ../war/$1
fi
cp patchserver-facade/build/libs/hotfix-apis.war ../war/$1/
cp patchserver-manager/build/libs/hotfix-console.war ../war/$1/

cp ../war/hotfix-apis.properties ../war/$1/
cp ../war/hotfix-console.properties ../war/$1/

cp ../war/readme.txt ../war/$1/