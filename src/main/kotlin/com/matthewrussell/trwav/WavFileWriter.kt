package com.matthewrussell.trwav

import sun.nio.cs.US_ASCII
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val BITS_PER_SAMPLE = 16
const val SAMPLE_RATE = 44100
const val NUM_CHANNELS = 1

class WavFileWriter(
    private val metadataMapper: MetadataMapper = MetadataMapper()
) {
    private fun wordAlignedLength(length: Int): Int {
        return length + (4 - length % 4)
    }
    private fun wordAlignString(str: String): String = str.padEnd(
        wordAlignedLength(str.length),
        0.toChar()
    )
    private fun makeMetadataChunk(metadata: Metadata): ByteArray {
        val metadataString = metadataMapper.toJSON(metadata)
        val paddedString = wordAlignString(metadataString)
        val buffer = ByteBuffer.allocate(paddedString.length + 20)
        buffer
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("LIST".toByteArray(US_ASCII()))
            .putInt(12 + paddedString.length)
            .put("INFOIART".toByteArray(US_ASCII()))
            .putInt(paddedString.length)
            .put(paddedString.toByteArray(US_ASCII()))
        return buffer.array()
    }
    private fun makeCueChunk(cues: List<CuePoint>): ByteArray {
        val cueSize = 24
        val cueChunkHeaderSize = 12
        val chunkSize = cueChunkHeaderSize + cueSize * cues.size
        val buffer = ByteBuffer.allocate(chunkSize)
        buffer
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("cue ".toByteArray(US_ASCII()))
            .putInt(chunkSize - cueChunkHeaderSize + 4)
            .putInt(cues.size)
        for (cue in cues) {
            buffer
                .putInt(cue.id)
                .putInt(cue.position)
                .put("data".toByteArray(US_ASCII()))
                .putInt(0)
                .putInt(0)
                .putInt(cue.position)
        }
        return buffer.array()
    }
    private fun makeLabelChunk(cues: List<CuePoint>): ByteArray {
        val size = (cues.size * 40) + 4 + cues.map { wordAlignedLength(it.label.length) }
            .reduce { acc, next -> acc + next }
        val buffer = ByteBuffer.allocate(size + 8)
        buffer
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("LIST".toByteArray(US_ASCII()))
            .putInt(size)
            .put("adtl".toByteArray(US_ASCII()))
        for (cue in cues) {
            buffer
                .put("ltxt".toByteArray(US_ASCII()))
                .putInt(20)
                .putInt(cue.id)
                .putInt(0)
                .put("rvn ".toByteArray(US_ASCII()))
                .putInt(0)
                .putInt(0)
                .put("labl".toByteArray(US_ASCII()))
                .putInt(4 + wordAlignedLength(cue.label.length))
                .putInt(cue.id)
                .put(wordAlignString(cue.label).toByteArray(US_ASCII()))
        }
        return buffer.array()
    }
    private fun makeHeader(size: Int): ByteArray {
        return ByteBuffer
            .allocate(44)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray(US_ASCII()))
            .putInt(size)
            .put("WAVEfmt ".toByteArray(US_ASCII()))
            .putInt(16)
            .putShort(1)
            .putShort(NUM_CHANNELS.toShort())
            .putInt(SAMPLE_RATE)
            .putInt(BITS_PER_SAMPLE * SAMPLE_RATE * NUM_CHANNELS / 8)
            .put((NUM_CHANNELS * BITS_PER_SAMPLE / 8).toByte())
            .put(0)
            .put(BITS_PER_SAMPLE.toByte())
            .put(0)
            .put("data".toByteArray(US_ASCII()))
            .putInt(size)
            .array()
    }
    fun write(data: WavFile, dest: File) {
        val metadataChunk = makeMetadataChunk(data.metadata)
        val labelChunk = makeLabelChunk(data.metadata.markers)
        val cueChunk = makeCueChunk(data.metadata.markers)
        val header = makeHeader(data.audio.size)
        if (!dest.exists()) dest.createNewFile()
        dest.outputStream().use {
            it.write(header)
            it.write(data.audio)
            it.write(cueChunk)
            it.write(labelChunk)
            it.write(metadataChunk)
        }
    }
}