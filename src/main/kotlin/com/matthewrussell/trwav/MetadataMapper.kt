package com.matthewrussell.trwav

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class MetadataMapper {
    private val mapper = ObjectMapper().registerKotlinModule()
    fun toJSON(metadata: Metadata): String = mapper.writeValueAsString(metadata)
    fun fromJSON(json: String): Metadata = mapper.readValue(json)
}