package com.spatial.format

import com.spatial.converter.*
import com.spatial.core.*

/**
 * PLY (Polygon File Format) ASCII reader/writer.
 * Supports: vertices with xyz, rgb, normals. Faces.
 * Does not support: binary format, custom properties.
 */
class PlyReader : FormatReader<PointCloud> {
    override val supportedExtensions = setOf("ply")
    
    override fun read(content: String): Result<PointCloud> = runCatching {
        val lines = content.lines()
        var lineIndex = 0
        
        // Parse header
        require(lines[lineIndex++].trim() == "ply") { "File must start with 'ply'" }
        
        var vertexCount = 0
        val properties = mutableListOf<String>()
        var inVertexElement = false
        
        while (lineIndex < lines.size) {
            val line = lines[lineIndex++].trim()
            if (line == "end_header") break
            
            val parts = line.split(WHITESPACE_REGEX)
            when {
                parts[0] == "element" && parts[1] == "vertex" -> {
                    vertexCount = parts[2].toInt()
                    inVertexElement = true
                }
                parts[0] == "element" -> inVertexElement = false
                parts[0] == "property" && inVertexElement -> {
                    properties.add(parts.last())
                }
            }
        }
        
        // Parse vertices
        val points = mutableListOf<Point3D>()
        val colors = mutableListOf<Color>()
        val normals = mutableListOf<Vector3D>()
        
        val xIdx = properties.indexOf("x")
        val yIdx = properties.indexOf("y")
        val zIdx = properties.indexOf("z")
        val rIdx = properties.indexOfFirst { it == "red" || it == "r" }
        val gIdx = properties.indexOfFirst { it == "green" || it == "g" }
        val bIdx = properties.indexOfFirst { it == "blue" || it == "b" }
        val nxIdx = properties.indexOf("nx")
        val nyIdx = properties.indexOf("ny")
        val nzIdx = properties.indexOf("nz")
        
        val hasColor = rIdx >= 0 && gIdx >= 0 && bIdx >= 0
        val hasNormals = nxIdx >= 0 && nyIdx >= 0 && nzIdx >= 0
        
        repeat(vertexCount) {
            val parts = lines[lineIndex++].trim().split(WHITESPACE_REGEX)
            
            points.add(Point3D(
                parts[xIdx].toDouble(),
                parts[yIdx].toDouble(),
                parts[zIdx].toDouble()
            ))
            
            if (hasColor) {
                colors.add(Color(
                    parts[rIdx].toInt(),
                    parts[gIdx].toInt(),
                    parts[bIdx].toInt()
                ))
            }
            
            if (hasNormals) {
                normals.add(Vector3D(
                    parts[nxIdx].toDouble(),
                    parts[nyIdx].toDouble(),
                    parts[nzIdx].toDouble()
                ))
            }
        }
        
        PointCloud(
            points = points,
            colors = if (hasColor) colors else null,
            normals = if (hasNormals) normals else null,
            metadata = PointCloudMetadata(sourceFormat = "ply")
        )
    }
    
    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}

class PlyWriter : FormatWriter<PointCloud> {
    override val supportedExtensions = setOf("ply")
    
    override fun write(data: PointCloud): Result<String> = runCatching {
        val hasColors = data.colors != null
        val hasNormals = data.normals != null
        
        buildString {
            // Header
            appendLine("ply")
            appendLine("format ascii 1.0")
            appendLine("element vertex ${data.size}")
            appendLine("property float x")
            appendLine("property float y")
            appendLine("property float z")
            if (hasNormals) {
                appendLine("property float nx")
                appendLine("property float ny")
                appendLine("property float nz")
            }
            if (hasColors) {
                appendLine("property uchar red")
                appendLine("property uchar green")
                appendLine("property uchar blue")
            }
            appendLine("end_header")
            
            // Data
            for (i in data.points.indices) {
                val p = data.points[i]
                append("${p.x} ${p.y} ${p.z}")
                
                if (hasNormals) {
                    val n = data.normals!![i]
                    append(" ${n.x} ${n.y} ${n.z}")
                }
                if (hasColors) {
                    val c = data.colors!![i]
                    append(" ${c.r} ${c.g} ${c.b}")
                }
                appendLine()
            }
        }
    }
}
