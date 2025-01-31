# Mobile Air Quality Monitor application for the SDS011 Sensor

This Android application is made for usage with the Nova Fitness
SDS011 Air Quality Sensor which should be connected to the mobile
phone using the
[OTG](https://en.wikipedia.org/wiki/USB_On-The-Go) cable.

## The sensor (active) mode of work

The application puts the sensor in so-called active mode (as opposed
to the query mode).

When in the active mode, the sensor sends data to the connected phone
without any additional queries using the defined period (which
can be continuous).

When in the query mode, the phone has to ask the sensor to sends
data for each measurement. This mode is not used by aqi-sds011
application.

## The application measurement period

Application is implemented to work in two modes:
- continuous
- periodic

In the continuous mode, the application receives new measurement
each second:
- new measurement is visible on the "Current data" screen
  each second
- multiple measurements are averaged and saved to history as
  defined on the "Settings" screen

In the periodic mode, the application receives new measurement
as defined by the work period (minutes):
- new measurement is visible on the "Current data" screen
  as defined by the "Settings" screen (minutes)
- each measurement is saved to history immediately

## How to build an app

App can be built and signed using a `gradle` command-line:
```bash
# Bundle is created: ./app/build/outputs/bundle/release/app-release.aab
./gradlew bundleRelease

# Signing a bundle
jarsigner -keystore <keystore_path.p12> ./app/build/outputs/bundle/release/app-release.aab <key_alias>
```