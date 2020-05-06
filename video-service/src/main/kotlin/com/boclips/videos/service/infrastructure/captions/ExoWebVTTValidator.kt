package com.boclips.videos.service.infrastructure.captions

import com.boclips.videos.service.application.collection.exceptions.InvalidWebVTTException
import com.boclips.videos.service.domain.service.video.CaptionValidator
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.util.regex.Matcher
import java.util.regex.Pattern


class ExoWebVTTValidator() : CaptionValidator {

    private val WEBVTT_FILE_HEADER_STRING = "^\uFEFF?WEBVTT((\\u0020|\u0009).*)?$"
    private val WEBVTT_FILE_HEADER: Pattern = Pattern.compile(WEBVTT_FILE_HEADER_STRING)
    private val WEBVTT_METADATA_HEADER_STRING: String = "\\S*[:=]\\S*"
    private val WEBVTT_METADATA_HEADER: Pattern = Pattern.compile(WEBVTT_METADATA_HEADER_STRING)
    private val WEBVTT_CUE_IDENTIFIER_STRING = "^(?!.*(-->)).*$"
    private val WEBVTT_CUE_IDENTIFIER: Pattern = Pattern.compile(WEBVTT_CUE_IDENTIFIER_STRING)
    private val WEBVTT_TIMESTAMP_STRING: String = "(\\d+:)?[0-5]\\d:[0-5]\\d\\.\\d{3}"
    private val WEBVTT_TIMESTAMP: Pattern = Pattern.compile(WEBVTT_TIMESTAMP_STRING)
    private val WEBVTT_NOTE_STRING: String = "NOTE"
    private val WEBVTT_NOTE: Pattern = Pattern.compile(WEBVTT_NOTE_STRING)

    override fun checkValid(content: String): Boolean {
        try {
            return parse(content).isNotEmpty()
        } catch (ex: Exception) {
            throw InvalidWebVTTException("Invalid web vtt file")
        }
    }

    override fun parse(content: String): ArrayList<String> {
        val lineBroken = content.replace("\\r\\n", System.lineSeparator())
        val webvttData = BufferedReader(InputStreamReader(ByteArrayInputStream(lineBroken.toByteArray(Charsets.UTF_8)), "UTF-8"))
        var line: String?

        // file should start with "WEBVTT"
        line = webvttData.readLine()
        if (line == null || !WEBVTT_FILE_HEADER.matcher(line).matches()) {
            throw InvalidWebVTTException("Expected WEBVTT. Got $line")
        }
        while (true) {
            line = webvttData.readLine()
            if (line == null) {
                // we reached EOF before finishing the header
                throw InvalidWebVTTException("Expected an empty line after webvtt header")
            } else if (line.isEmpty()) {
                // we've read the newline that separates the header from the body
                break
            } else if(line.isNotEmpty()) {
                val matcher: Matcher = WEBVTT_METADATA_HEADER.matcher(line)
                if (!matcher.find()) {
                    throw InvalidWebVTTException("Expected WebVTT metadata header. Got " + line);
                }
            }
        }

        val captionContents = ArrayList<String>()
        // process the cues and text
        while (webvttData.readLine().also { line = it } != null) {
            if ("" == line!!.trim { it <= ' ' }) {
                continue
            }
            // parse the note (if present) {
            var matcher: Matcher = WEBVTT_NOTE.matcher(line)
            if (matcher.find()) {
                // ignore the note and the blank line under
                webvttData.readLine()
                line = webvttData.readLine()
            }

            matcher = WEBVTT_CUE_IDENTIFIER.matcher(line)
            if (matcher.find()) {
                // ignore the identifier (we currently don't use it) and read the next line
                line = webvttData.readLine()
            }

            // parse the cue timestamps
            matcher = WEBVTT_TIMESTAMP.matcher(line)

            if (!matcher.find()) {
                throw InvalidWebVTTException("Expected cue start time: " + line);
            }

            // parse end timestamp
            var endTimeString: String
            if (!matcher.find()) {
                throw InvalidWebVTTException("Expected cue end time: " + line);
            } else {
                endTimeString = matcher.group()
            }

            line = line!!.substring(line!!.indexOf(endTimeString) + endTimeString.length)

            while (webvttData.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                captionContents.add(line!!.trim { it <= ' ' })
            }
        }
        return captionContents
    }

}
