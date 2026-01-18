package com.spatial.format

import com.spatial.converter.*
import com.spatial.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JSON serialization using kotlinx.serialization.
 * Useful for web APIs and debugging.
 */
class JsonPointCloudWriter(
    private val prettyPrint: Boolean = false
) : FormatWriter<PointCloud> {
    override val supportedExtensions = setOf("json")
    
    private val json = Json { 
        this.prettyPrint = this@JsonPointCloudWriter.prettyPrint
        encodeDefaults = false
    }
    
    override fun write(data: PointCloud): Result<String> = runCatching {
        json.encodeToString(data)
    }
}

class JsonPointCloudReader : FormatReader<PointCloud> {
    override val supportedExtensions = setOf("json")
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun read(content: String): Result<PointCloud> = runCatching {
        json.decodeFromString<PointCloud>(content)
    }
}

class JsonMeshWriter(
    private val prettyPrint: Boolean = false
) : FormatWriter<Mesh> {
    override val supportedExtensions = setOf("json")
    
    private val json = Json { 
        this.prettyPrint = this@JsonMeshWriter.prettyPrint
        encodeDefaults = false
    }
    
    override fun write(data: Mesh): Result<String> = runCatching {
        json.encodeToString(data)
    }
}

class JsonMeshReader : FormatReader<Mesh> {
    override val supportedExtensions = setOf("json")
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun read(content: String): Result<Mesh> = runCatching {
        json.decodeFromString<Mesh>(content)
    }
}
