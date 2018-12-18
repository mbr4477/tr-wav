package com.matthewrussell.trwav

data class WavFile(
    val metadata: Metadata = Metadata(),
    var audio: ByteArray = ByteArray(0)
)