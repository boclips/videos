package com.boclips.videos.service.testsupport

fun loadFile(filename: String) = Object::getClass.javaClass.classLoader.getResource(filename).readBytes()
