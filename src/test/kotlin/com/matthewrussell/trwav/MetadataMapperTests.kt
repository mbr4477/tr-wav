package com.matthewrussell.trwav

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.Assert
import org.junit.Test

class MetadataMapperTests {
    private val TO_JSON_TESTS = listOf(
        Pair(
            Metadata("nt", "en", "12", "mat", "28", "chunk", "1", "1", "2", "Contributor", mutableListOf(CuePoint(0, "1"), CuePoint(40000, "2"))),
            "{\"anthology\":\"nt\",\"language\":\"en\",\"version\":\"12\",\"slug\":\"mat\",\"book_number\":\"28\",\"mode\":\"chunk\",\"chapter\":\"1\",\"startv\":\"1\",\"endv\":\"2\",\"contributor\":\"Contributor\",\"markers\":{\"1\":0,\"2\":40000}}"
        ),
        Pair(
            Metadata("ot", "fr", "1.1", "gen", "1", "verse", "2", "3", "3", "Author", mutableListOf(CuePoint(0, "3"))),
            "{\"anthology\":\"ot\",\"language\":\"fr\",\"version\":\"1.1\",\"slug\":\"gen\",\"book_number\":\"1\",\"mode\":\"verse\",\"chapter\":\"2\",\"startv\":\"3\",\"endv\":\"3\",\"contributor\":\"Author\",\"markers\":{\"3\":0}}"
        )
    )

    private val FROM_JSON_TESTS = listOf(
        Pair(
            Metadata("nt", "en", "12", "mat", "28", "chunk", "1", "1", "2", "Contributor", mutableListOf(CuePoint(0, "1"), CuePoint(40000, "2"))),
            "{\"anthology\":\"nt\",\"language\":\"en\",\"version\":\"12\",\"slug\":\"mat\",\"book_number\":\"28\",\"mode\":\"chunk\",\"chapter\":\"1\",\"startv\":\"1\",\"endv\":\"2\",\"contributor\":\"Contributor\",\"markers\":{\"1\":0,\"2\":40000}}"
        ),
        Pair(
            Metadata("ot", "fr", "1.1", "gen", "1", "verse", "2", "3", "3", "Author", mutableListOf(CuePoint(0, "3"))),
            "{\"anthology\":\"ot\",\"language\":\"fr\",\"version\":\"1.1\",\"book\":\"gen\",\"book_number\":\"1\",\"mode\":\"verse\",\"chapter\":\"2\",\"startv\":\"3\",\"endv\":\"3\",\"contributor\":\"Author\",\"markers\":{\"3\":0}}"
        )
    )

    private val MISSING_FIELDS_CASES = listOf(
        Pair(
            "{}",
            Metadata("", "", "", "", "", "", "", "", "", "", mutableListOf())
        )
    )

    private val EXTRA_FIELDS_CASES = listOf(
        Pair(
            "{\"extra_key\":\"Extra Value\",\"anthology\":\"ot\",\"language\":\"fr\",\"version\":\"1.1\",\"book\":\"gen\",\"book_number\":\"1\",\"mode\":\"verse\",\"chapter\":\"2\",\"startv\":\"3\",\"endv\":\"3\",\"contributor\":\"Author\",\"markers\":{\"3\":0}}",
            Metadata("ot", "fr", "1.1", "gen", "1", "verse", "2", "3", "3", "Author", mutableListOf(CuePoint(0, "3")))
        )
    )

    private val mapper = MetadataMapper()

    @Test
    fun shouldMapMetadataToJSON() {
        for (test in TO_JSON_TESTS) {
            val input = test.first
            val expected = test.second
            val result = mapper.toJSON(input)
            Assert.assertEquals(expected, result)
        }
    }

    @Test
    fun shouldMapJSONToMetadata() {
        for (test in FROM_JSON_TESTS) {
            val input = test.second
            val expected = test.first
            val result = mapper.fromJSON(input)
            Assert.assertEquals(expected, result)
        }
    }

    @Test
    fun shouldThrowExceptionIfExtraFields() {
        for (test in EXTRA_FIELDS_CASES) {
            val input = test.first
            try {
                val result = mapper.fromJSON(input)
                Assert.fail()
            } catch (e: UnrecognizedPropertyException) {
                // everything okay
            }
        }
    }

    @Test
    fun shouldUseDefaultsForMissingFields() {
        for (test in MISSING_FIELDS_CASES) {
            val input = test.first
            val expected = test.second
            val result = mapper.fromJSON(input)
            Assert.assertEquals(expected, result)
        }
    }
}