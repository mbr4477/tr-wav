package com.matthewrussell.trwav

import sun.nio.cs.US_ASCII
import java.io.File
import java.io.RandomAccessFile
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val WAV_HEADER_LENGTH = 44

class WavFileReader(
    private val metadataMapper: MetadataMapper = MetadataMapper()
) {
    private var audioByteCount = 0
    private var metadata: Metadata = Metadata()
    private var cues: HashMap<Int, CuePoint> = hashMapOf()
    private var audioData: ByteArray = ByteArray(0)

    private fun readLabel(buffer: ByteBuffer): String {
        val label = ByteArray(4)
        buffer.get(label)
        return label.toString(US_ASCII())
    }

    private fun readFile(file: RandomAccessFile, readAudio: Boolean = true) {
        file.seek(0)
        val riffLabel = ByteArray(4)
        file.read(riffLabel)
        if (riffLabel.toString(US_ASCII()) != "RIFF") throw RuntimeException("Not a WAV file")
        file.seek(40)
        val audioLength = file.readIntLE().toLong()
        audioByteCount = audioLength.toInt()

        // Read the audio
        if (readAudio) {
            audioData = ByteArray(audioLength.toInt())
            file.read(audioData)
        }

        // Go find the metadata
        file.seek(WAV_HEADER_LENGTH + audioLength)
        val metadataBytes = ByteArray((file.length() - audioLength - WAV_HEADER_LENGTH).toInt())
        file.read(metadataBytes)
        val metadataBuffer = ByteBuffer.wrap(metadataBytes).order(ByteOrder.LITTLE_ENDIAN)
        while (metadataBuffer.remaining() > 8) {
            val label = readLabel(metadataBuffer)
            val chunkSize = metadataBuffer.int
            if (chunkSize > metadataBuffer.remaining()) throw RuntimeException("Chunk size larger than remaining file length")
            val chunk = metadataBuffer.slice().order(ByteOrder.LITTLE_ENDIAN).limit(chunkSize) as ByteBuffer
            metadataBuffer.position(metadataBuffer.position() + chunkSize)
            when (label) {
                "LIST" -> readList(chunk)
                "cue " -> readCues(chunk)
            }
        }
        metadata.markers.sortBy { it.position }
    }

    private fun readList(chunk: ByteBuffer) {
        while (chunk.position() < chunk.limit()) {
            val label = readLabel(chunk)
            when (label) {
                "adtl" -> {
                    readLabels(chunk.slice().order(ByteOrder.LITTLE_ENDIAN))
                    return
                }
                "INFO" -> {
                    val infoLabel = readLabel(chunk)
                    val chunkSize = chunk.int
                    if (chunkSize > chunk.remaining()) throw RuntimeException("Subchunk size of list is invalid")
                    when (infoLabel) {
                        "IART" -> {
                            val trMetadataBytes = ByteArray(chunkSize)
                            chunk.get(trMetadataBytes)
                            val jsonString = trMetadataBytes.toString(US_ASCII())
                            metadata = metadataMapper.fromJSON(jsonString)
                            // parse the metadata data
                        }
                        else -> chunk.position(chunk.position() + chunkSize)
                    }
                }
            }
        }
    }

    private fun readCues(chunk: ByteBuffer) {
        if (!chunk.hasRemaining()) return
        val cueCount = chunk.int
        if (chunk.remaining() != (24 * cueCount)) return
        for (i in 0 until cueCount) {
            val id = chunk.int
            val position = chunk.int
            if (cues.keys.contains(id)) {
                cues[id]?.position = position
            } else {
                cues[id] = CuePoint(position, id = id)
            }
            chunk.position(chunk.position() + 16)
        }
    }

    private fun readLabels(chunk: ByteBuffer) {
        while (chunk.hasRemaining()) {
            val label = readLabel(chunk)
            when (label) {
                "ltxt" -> {
                    val size = chunk.int
                    chunk.position(chunk.position() + size)
                    val subLabel = readLabel(chunk)
                    val subSize = chunk.int
                    when (subLabel) {
                        "labl" -> {
                            val id = chunk.int
                            val labelBytes = ByteArray(subSize - 4)
                            chunk.get(labelBytes)
                            val cueLabel = labelBytes.toString(US_ASCII()).trim(0.toChar())
                            if (cues.keys.contains(id)) {
                                cues[id]?.label = cueLabel
                            } else {
                                cues[id] = CuePoint(label = cueLabel, id = id)
                            }
                        }
                        else -> {
                            chunk.position(chunk.position() + subSize)
                        }
                    }
                }
            }
        }
    }

    private fun matchCuePoints() {
        // Match labels and cue point ids
        metadata.markers.forEach { cue ->
            if (cues.filter { it.value.label == cue.label }.isNotEmpty()) {
                cue.id = cues.filter { it.value.label == cue.label }.values.first().id
            } else {
                cue.id = (cues.keys.max() ?: 0) + 1
            }
        }
    }

    fun read(wav: File): WavFile {
        cues = hashMapOf()
        val file = RandomAccessFile(wav, "r")
        readFile(file)
        matchCuePoints()
        return WavFile(metadata, audioData)
    }

    fun readMetadata(wav: File): Metadata {
        cues = hashMapOf()
        val file = RandomAccessFile(wav, "r")
        readFile(file, false)
        matchCuePoints()
        return metadata
    }

    fun duration(wav: File): Double {
        readMetadata(wav)
        return audioByteCount / (BITS_PER_SAMPLE / 8) / 44100.0
    }
}