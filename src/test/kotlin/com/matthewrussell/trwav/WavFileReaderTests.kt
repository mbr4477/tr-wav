package com.matthewrussell.trwav

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.lang.RuntimeException

class WavFileReaderTests {
    private val testFile = File(ClassLoader.getSystemResource("en_ulb_b63_1jn_c03_v01-03_t04.wav").toURI())
    private val plainWav = File(ClassLoader.getSystemResource("plain_wav.wav").toURI())
    private val notWav = File(ClassLoader.getSystemResource("not_wav.txt").toURI())

    @Test
    fun shouldGetFileDuration() {
        val actual = WavFileReader().duration(testFile)
        val expected =  34.5938
        Assert.assertEquals(expected, actual, 0.0001)
    }

    @Test
    fun shouldReadMetadata() {
        val expected = Metadata(
            "nt",
            "en",
            "ulb",
            "1jn",
            "63",
            "chunk",
            "3",
            "1",
            "3",
            "",
            mutableListOf(
                CuePoint(0, "1", 1),
                CuePoint(537586, "2", 2),
                CuePoint(1168141, "3", 3)
            )
        )
        val actual = WavFileReader().readMetadata(testFile)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun shouldReadMetadataAndAudio() {
        val expectedMetadata = Metadata(
            "nt",
            "en",
            "ulb",
            "1jn",
            "63",
            "chunk",
            "3",
            "1",
            "3",
            "",
            mutableListOf(
                CuePoint(0, "1", 1),
                CuePoint(537586, "2", 2),
                CuePoint(1168141, "3", 3)
            )
        )
        val actual = WavFileReader().read(testFile)
        val expectedBytes = 1525587 * BITS_PER_SAMPLE / 8
        val actualBytes = actual.audio.size
        Assert.assertEquals(expectedMetadata, actual.metadata)
        Assert.assertEquals(expectedBytes, actualBytes)
    }

    @Test(expected = RuntimeException::class)
    fun shouldThrowExceptionIfNotWavFile() {
        WavFileReader().read(notWav)
    }

    @Test
    fun shouldHandlePlainWavFile() {
        val expectedMetadata = Metadata(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            mutableListOf()
        )
        val actual = WavFileReader().read(plainWav)
        val expectedBytes = 44100
        val actualBytes = actual.audio.size
        Assert.assertEquals(expectedMetadata, actual.metadata)
        Assert.assertEquals(expectedBytes, actualBytes)
    }
}