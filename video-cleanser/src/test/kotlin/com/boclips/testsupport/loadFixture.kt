package com.boclips.testsupport

fun loadFixture(fileName: String) = Object::getClass.javaClass.classLoader.getResource(fileName).readBytes()