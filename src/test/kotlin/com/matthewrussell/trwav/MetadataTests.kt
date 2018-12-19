package com.matthewrussell.trwav

import org.junit.Assert
import org.junit.Test
import java.lang.IllegalArgumentException

class MetadataTests {
    @Test
    fun shouldMakeVerseFilename() {
        val input = Metadata(
            "nt",
            "fr",
            "udb",
            "mat",
            "28",
            "verse",
            "10",
            "1",
            "1",
            "Contributor",
            mutableListOf()
        )
        val inputTakeInfo = "t10"

        val expected = "fr_udb_b28_mat_c10_v01_t10.wav"
        val result = input.toFilename(inputTakeInfo)
        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldMakeChunkFilename() {
        val input = Metadata(
            "ot",
            "en",
            "ulb",
            "gen",
            "1",
            "chunk",
            "1",
            "1",
            "10",
            "Contributor",
            mutableListOf()
        )
        val inputTakeInfo = "t04"

        val expected = "en_ulb_b01_gen_c01_v01-10_t04.wav"
        val result = input.toFilename(inputTakeInfo)
        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldMakeChunkFilenameWithPaddedChapterAndVerse() {
        val input = Metadata(
            "ot",
            "en",
            "ulb",
            "psa",
            "19",
            "chunk",
            "19",
            "1",
            "2",
            "Contributor",
            mutableListOf()
        )
        val inputTakeInfo = "t01"
        val width = 3

        val expected = "en_ulb_b19_psa_c019_v001-002_t01.wav"
        val result = input.toFilename(inputTakeInfo, width, width)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionIfNegativeWidth() {
        val input = Metadata(
            "ot",
            "en",
            "ulb",
            "psa",
            "19",
            "chunk",
            "19",
            "1",
            "2",
            "Contributor",
            mutableListOf()
        )
        val inputTakeInfo = "t01"
        val width = -1
        input.toFilename(inputTakeInfo, width, width)
    }
}