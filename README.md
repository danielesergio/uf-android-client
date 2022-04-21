<h2 align="center">UFAndroidClient</h2>
<p align="center">
<a href="https://travis-ci.org/Kynetics/uf-android-client"><img alt="Build Status" src="https://travis-ci.org/Kynetics/uf-android-client.svg?branch=master"></a>
<a href="https://codeclimate.com/github/Kynetics/uf-android-client/maintainability"><img src="https://api.codeclimate.com/v1/badges/3dcb8f7ce1c2a6c9f9e2/maintainability" /></a>
<a href="http://www.eclipse.org/legal/epl-v10.html"><img alt="License" src="https://img.shields.io/badge/License-EPL%201.0-red.svg"></a>
<a href="https://jitpack.io/#kynetics/uf-android-client"><img alt="GitHub release (latest SemVer)" src="https://img.shields.io/github/v/release/kynetics/uf-android-client"></a>
</p>

UFAndroidClient is an Android application that applies app (apk) and system (OTA) software updates received from an [UpdateFactory](https://www.kynetics.com/iot-platform-update-factory) or [Hawkbit](https://eclipse.org/hawkbit/) server.

Links to official documentation:
- [installation](https://docs.updatefactory.io/devices/android/android-client-packages/)
- [configuration file](https://docs.updatefactory.io/devices/android/android-config-files/)
- [third-party integration](https://docs.updatefactory.io/devices/android/third-party-integration-v1/)
- [kdocs API](https://kynetics.github.io/uf-android-client/)

## Build
To build this project the Android SDK Platform `android.jar` should be replaced with the Android Hidden API `android.jar` file as described in [Android Hidden API README](https://github.com/anggrayudi/android-hidden-api).
On Linux the [installation script](install_android-hidden-api-jar.sh) can be used to automatically install the `android.jar`.

**This is the [android.jar](https://github.com/Reginer/aosp-android-jar/blob/main/android-30/android.jar) 
to use because of this [issue](https://github.com/anggrayudi/android-hidden-api/issues/84)**

## uf-client-service
uf-client-service is an android service that run in background and manage the updates.

uf-client-service must be install as **SYSTEM** application. [Android hidden api](https://github.com/anggrayudi/android-hidden-api) 
are used from the version 1.0 to support update of systems with double partitions.

### State diagrams
#### Main
![UF STM Main](https://drive.google.com/uc?export=view&id=1g8r0gk7tNlrCbquzMlhXmDDGMxYc6kxT)
#### Update
![UF STM Update](https://drive.google.com/uc?export=view&id=1-EWX7pIpEWcBf3RFFW8MBhleooD8Nbp8)

## uf-client-ui-example
Uf-client-ui-example is an example of application that use the uf-client-service

## Third-Party Libraries
* [uf-ddiclient](https://github.com/Kynetics/uf-ddiclient) library

## Authors
* **Daniele Sergio** - *Initial work* - [danielesergio](https://github.com/danielesergio)
* **Andrea Zoleo** 
* **Diego Rondini**

See also the list of [contributors](https://github.com/Kynetics/UfAndroidClient/graphs/contributors) who participated in this project.

## License
Copyright © 2017-2020, [Kynetics LLC](https://www.kynetics.com).
Released under the [EPLv1 License](http://www.eclipse.org/legal/epl-v10.html).
