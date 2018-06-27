#!/usr/bin/env bash


../gradlew build
SPRING_PROFILES_ACTIVE=production java -jar build/libs/video-analyser*.jar