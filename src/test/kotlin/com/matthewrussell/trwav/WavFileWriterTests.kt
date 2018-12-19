package com.matthewrussell.trwav

import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.io.File

class WavFileWriterTests {
    private val outputFile = File("output.wav")
    @Test
    fun shouldWriteFileWithMetadata() {
        val metadata = Metadata(
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
        val audioBytes = ByteArray(64)
        val wavFile = WavFile(metadata, audioBytes)
        WavFileWriter().write(wavFile, outputFile)

        val readFile = WavFileReader().read(outputFile)
        Assert.assertEquals(wavFile.metadata, readFile.metadata)
        Assert.assertEquals(wavFile.audio.size, readFile.audio.size)
    }

    @After
    fun tearDown() {
        outputFile.delete()
    }
}