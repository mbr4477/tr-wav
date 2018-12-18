package com.matthewrussell.trwav

import java.io.EOFException
import java.io.RandomAccessFile

fun RandomAccessFile.readIntLE(): Int {
    val ch1 = read()
    val ch2 = read()
    val ch3 = read()
    val ch4 = read()
    if (ch1.or(ch2).or(ch3).or(ch4) < 0) throw EOFException()
    return ch4.shl(24) + ch3.shl(16) + ch2.shl(8) + ch1
}