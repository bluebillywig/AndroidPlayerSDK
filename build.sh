#!/bin/bash
rm -rf build
mkdir build

BBPLAYER=`find bbcomponent | grep BBPlayer.java`
VERSION=`cat ${BBPLAYER} | grep 'String VERSION' | awk '{ print $6 }' | sed 's/[";]//g'`

GRADLE_LOCATION="'com.github.bluebillywig:BlueBillywigPlayerSDK:$VERSION'"

echo $VERSION

./gradlew build &&
cp bbcomponent/build/outputs/aar/bbcomponent-release.aar app/libs &&

rm Blue_Billywig_Android_Component-*.zip
sed -i .orig "s/\(.*implementation \)project(path: ':bbcomponent').*/\1${GRADLE_LOCATION}/" app/build.gradle
cat app/build.gradle | grep implementation
sed -i .orig 's/,.*//' settings.gradle

zip -r Blue_Billywig_Android_Component-v$VERSION.zip settings.gradle gradle build.gradle .idea/* app/libs app/app.iml app/build.gradle app/lint.xml app/src --exclude "*.DS_Store" --exclude "*modules.xml" --exclude "*workspace.xml" --exclude "*/libraries*" --exclude "*/caches*" > /dev/null 2>&1

rm -rf BlueBillywigAndroidComponent
mkdir BlueBillywigAndroidComponent
cd BlueBillywigAndroidComponent
unzip ../Blue_Billywig_Android_Component-v$VERSION.zip
cd ..

rm Blue_Billywig_Android_Component-v$VERSION.zip
zip -r Blue_Billywig_Android_Component-v$VERSION.zip BlueBillywigAndroidComponent

cp app/build.gradle.orig app/build.gradle
cp settings.gradle.orig settings.gradle

echo
echo "To upload to the support site:"
echo "scp Blue_Billywig_Android_Component-*.zip bb@52.31.254.189:/var/www/support.bluebillywig.com/supportdocs/"
echo
echo "Do this after this library has been pushed to gradle, and is available on sonatype https://knowhow.bluebillywig.com/kb/android-gradle-sdk-library-support/"
