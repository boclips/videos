package com.boclips.api

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class RegexMatcher(private val regex: String) : TypeSafeMatcher<String>() {

    override fun describeTo(description: Description) {
        description.appendText("matches regular expression=`$regex`")
    }

    public override fun matchesSafely(string: String): Boolean {
        return string.matches(regex.toRegex())
    }

    companion object {

        fun matchesRegex(regex: String): RegexMatcher {
            return RegexMatcher(regex)
        }
    }
}