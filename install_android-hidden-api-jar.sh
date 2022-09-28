#
# Copyright © 2017-2022  Kynetics  LLC
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

if [ -z "${ANDROID_HOME}" ] ; then
	echo "ANDROID:HOME is not set. Please set it and try again"
	echo "https://developer.android.com/studio/command-line/variables"
	exit 1
fi

ANDROID_JAR_PATH="${ANDROID_HOME}/platforms/android-30/android.jar"

if [ ! -f "${ANDROID_JAR_PATH}" ] ; then
	echo "Could not find ${ANDROID_JAR_PATH}, please install Android SDK Platform 30"
	exit 1
fi

# Backup original
echo ">> Performing backup of original android.jar"
cp -v "${ANDROID_JAR_PATH}" "${ANDROID_JAR_PATH}.orig"

# Download Android Hidden API android.jar
echo ">> Downloading Android Hidden API android.jar"
curl -o "${ANDROID_JAR_PATH}" https://raw.githubusercontent.com/Reginer/aosp-android-jar/main/android-30/android.jar
retVal=$?
if [ $retVal -ne 0 ]; then
	echo "Failed to download Android Hidden API android.jar"
	exit 1
fi

# Verify checksum
echo ">> Verifying checksum of Android Hidden API android.jar"
echo "46b4ca17ea7b2372c8ce3530a731b0c3  ${ANDROID_JAR_PATH}" | md5sum -c
retVal=$?
if [ $retVal -ne 0 ]; then
	echo "Checksum does not match, file changed or download is corrupted"
	exit 1
fi

echo ">> Successfully installed Android Hidden API android.jar"
