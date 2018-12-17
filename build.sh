#!/bin/bash
LIBNAME=bbcomponent
rm -rf build
mkdir build

cd bbcomponent
BBPLAYER=`find . | grep BBPlayer.java`
VERSION=`cat ${BBPLAYER} | grep 'String VERSION' | awk '{ print $6 }' | sed 's/[";]//g'`
../gradlew build
cp build/outputs/aar/bbcomponent-release.aar ../app/libs
cd ..

rm Blue_Billywig_Android_Component-v$VERSION.zip
sed -i .orig "s/\(.*\)compile project.*/\1compile (name:'bbcomponent-release', ext:'aar')/" app/build.gradle
sed -i .orig 's/,.*//' settings.gradle

cd ..

zip -r BBWebview/Blue_Billywig_Android_Component-v$VERSION.zip BBWebview/settings.gradle BBWebview/build.gradle BBWebview/local.properties BBWebview/.idea BBWebview/app/libs BBWebview/app/app.iml BBWebview/app/build.gradle BBWebview/app/lint.xml BBWebview/app/src --exclude "*.DS_Store" --exclude "*modules.xml" --exclude "*workspace.xml"

cd BBWebview
cp app/build.gradle.orig app/build.gradle
cp settings.gradle.orig settings.gradle
