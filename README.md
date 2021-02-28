# Mobile Air Quality Monitor application for the SDS011 Sensor

This Android application is made for usage with the Nova Fitness
SDS011 Air Quality Sensor which should be connected to the mobile
phone using the
[OTG](https://en.wikipedia.org/wiki/USB_On-The-Go) cable.

## The sensor (active) mode of work

Application puts sensor in so called active mode (as opposed to
the query mode).

When in the active mode, sensor sends data to the connected phone
without any additional queries using the defined period (which
can be continuous).

When in the query mode, phone have to ask the sensor to sends
data for each measurement. This mode is not used by aqi-sds011
application.

## The application measurement period

Application is implement to work in two modes:
- continuous
- periodic

In the continuous mode, application receives new measurement
each second:
- new measurement is visible on the "Current data" screen
  each second
- multiple measurements are averaged and saved to history as
  defined on the "Settings" screen

In the periodic mode, application receives new measurement
as defined by the work period (minutes):
- new measurement is visible on the "Current data" screen
  as defined by the "Settings" screen (minutes)
- each measurement is saved to history immediately
