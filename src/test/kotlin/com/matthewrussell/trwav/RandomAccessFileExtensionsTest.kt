package com.matthewrussell.trwav

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile

class RandomAccessFileExtensionsTest {
    val testFile = File("test.bin")

    @Before
    fun setup() {
        testFile.createNewFile()
        testFile.writeBytes(
            listOf(0x01, 0x02, 0x03, 0x04)
                .map { it.toByte() }
                .toByteArray()
        )
    }

    @After
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun shouldReadIntLE() {
        val raf = RandomAccessFile(testFile, "r")
        val expected = 0x04030201
        val actual = raf.readIntLE()
        Assert.assertEquals(expected, actual)
    }

    @Test(expected = EOFException::class)
    fun shouldThrowEOF() {
        val raf = RandomAccessFile(testFile, "r")
        raf.readIntLE()
        raf.readIntLE()
    }
}