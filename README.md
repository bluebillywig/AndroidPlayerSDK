# Android player SDK

Before releasing update in bbcomponent/src/main/java/com/bluebillywig/BBPlayer.java the variable VERSION to the version you are going to deploy, for example: "1.4.18"
Now run the

    ./build.sh

to build and stage the new release.

Remember to copy the created zip file to the support site and update the link by logging in to: https://support.bluebillywig.com/wp-admin and edit the https://support.bluebillywig.com/mobile-sdks/android-player-sdk-guide/ page to link to the new version.

Go to https://s01.oss.sonatype.org/#stagingRepositories and see the release appear.
Select the release and press on the "Close" button.
Wait for a moment and press "Refresh" until the "Release" button is enabled.
Now press on the "Release" button.

The new release will appear on https://search.maven.org/search?q=a:BlueBillywigPlayerSDK, but it can take at least a couple of hours.
