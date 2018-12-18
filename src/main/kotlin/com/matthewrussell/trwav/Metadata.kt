package com.matthewrussell.trwav

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class Metadata(
    var anthology: String = "",
    var language: String = "",
    var version: String = "",
    var book: String = "",
    @JsonProperty("book_number")
    var bookNumber: String = "",
    var mode: String = "",
    var chapter: String = "",
    var startv: String = "",
    var endv: String = "",
    var contributor: String = "",
    @JsonDeserialize(using = MarkerListDeserializer::class)
    @JsonSerialize(using = MarkerListSerializer::class)
    var markers: MutableList<CuePoint> = mutableListOf()
) {
    fun toFilename(resourceSlug: String, takeInfo: String, chapterWidth: Int = 2, verseWidth: Int = 2): String {
        val paddedStartV = startv.padStart(verseWidth, '0')
        val paddedEndV = endv.padStart(verseWidth, '0')
        return listOf(
            language,
            resourceSlug,
            "b${bookNumber.padStart(2, '0')}",
            book,
            "c${chapter.padStart(chapterWidth, '0')}",
            if (startv != endv) "v$paddedStartV-$paddedEndV" else "v$paddedStartV",
            takeInfo
        ).joinToString("_").plus(".wav")
    }
}