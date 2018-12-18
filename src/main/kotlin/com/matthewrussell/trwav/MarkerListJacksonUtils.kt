package com.matthewrussell.trwav

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import java.lang.IllegalArgumentException
import java.text.ParseException

class MarkerListDeserializer : JsonDeserializer<List<CuePoint>>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): List<CuePoint> {
        val node = p?.codec?.readTree<JsonNode>(p) ?: throw ParseException("Unable to parse json", 0)
        val cuePoints = mutableListOf<CuePoint>()
        for (key in node.fieldNames()) {
            cuePoints.add(CuePoint(node.get(key).asInt(), key))
        }
        return cuePoints
    }
}

class MarkerListSerializer : JsonSerializer<List<CuePoint>>() {
    override fun serialize(value: List<CuePoint>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value == null) throw IllegalArgumentException("Input cannot be null")
        gen?.writeStartObject()
        for (cue in value) {
            gen?.writeNumberField(cue.label, cue.position)
        }
        gen?.writeEndObject()
    }
}