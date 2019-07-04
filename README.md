# aare-server

[![Build Status](https://travis-ci.org/guggero/aare-server.svg?branch=master)](https://travis-ci.org/guggero/aare-server)

## Overview

This is the new Spring Boot implementation of the backend for the iOS app "Aare" which shows the temperature of the river Aare in Switzerland.

It fetches the hydrological data from [BAFU](http://www.hydrodaten.admin.ch/de), caches it in an MySQL database and then composes it so the app can use it.

Features:
* New implementation in Spring Boot 2 (was Java EE 7 before and Grails before that)
* Dockerized, pushed to docker hub [guggero/aare-api](https://cloud.docker.com/repository/docker/guggero/aare-api)
* Maven build
* Travis CI integration
