[![](https://jitpack.io/v/mbr4477/tr-wave.svg)](https://jitpack.io/#mbr4477/tr-wave)

# tr-wav
A Kotlin library for working with tR WAV audio file metadata.

## Gradle Usage
Add the following repository to `build.gradle`.
```groovy
maven { url 'https://jitpack.io' }
```
Add the dependency:
```groovy
implementation 'com.github.mbr4477:tr-wave:master-SNAPSHOT'
```
## Example
```kotlin
import com.matthewrussell.trwav.*

fun main(args: Array<String>) {
    // Read in a file
    val wavFile: WavFile = WavFileReader().read(wavFile)
    // Print the file's tR metadata
    println(wavFile.metadata)
    // Update the metadata
    waveFile.metadata.book = "gen"
    // Write the file back out
    WavFileWriter().write(wavFile, File("output.wav"))
}
```