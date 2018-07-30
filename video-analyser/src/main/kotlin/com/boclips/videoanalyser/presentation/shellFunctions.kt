package com.boclips.videoanalyser.presentation

import java.io.InputStreamReader
import java.io.LineNumberReader

fun say(aThing: String) = System.out.println("\n$aThing")

fun ask(question: String): String? {
    System.out.println("\n$question")
    return LineNumberReader(InputStreamReader(System.`in`)).readLine()
}

fun askYesNo(question: String): Boolean {
    while (true) {
        when (ask(String.format("%s (y/n): ", question))) {
            "y" -> return true
            "n" -> return false
        }
    }
}
