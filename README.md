# aare-server

[![Build Status](https://travis-ci.org/guggero/aare-server.svg?branch=master)](https://travis-ci.org/guggero/aare-server)

## Overview

This is the new Java EE 7 implementation of the backend for the iOS app "Aare" which shows the temperature of the river Aare in Switzerland.

It fetches the hydrological data from [BAFU](http://www.hydrodaten.admin.ch/de), caches it in an MySQL database and then composes it so the app can use it.

Features:
* New implementation in "plain" Java EE 7 (was Grails before)
* Dockerizied
* Maven build
* Travis CI integration
