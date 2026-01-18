package com.spatial.core

import kotlinx.serialization.Serializable

/**
 * Mesh representation using indexed vertex storage for memory efficiency.
 * Vertices are stored once and referenced by index in faces.
 */
@Serializable
data class Mesh(
    val vertices: List<Point3D>,
    val faces: List<Face>,
    val normals: List<Vector3D> = emptyList(),
    val metadata: MeshMetadata = MeshMetadata()
) {
    val triangleCount: Int get() = faces.sumOf { it.triangleCount }
    val boundingBox: BoundingBox by lazy { BoundingBox.fromPoints(vertices) }
    
    fun triangles(): Sequence<Triangle3D> = sequence {
        for (face in faces) {
            yieldAll(face.toTriangles(vertices))
        }
    }
    
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val vertexCount = vertices.size
        
        for ((faceIndex, face) in faces.withIndex()) {
            for (vertexIndex in face.indices) {
                if (vertexIndex < 0 || vertexIndex >= vertexCount) {
                    errors.add("Face $faceIndex references invalid vertex index $vertexIndex")
                }
            }
        }
        
        return errors
    }
}

/**
 * Face with arbitrary vertex count. Supports triangles, quads, and n-gons.
 * Uses fan triangulation for n-gons (first vertex is pivot).
 */
@Serializable
data class Face(val indices: List<Int>) {
    init {
        require(indices.size >= 3) { "Face must have at least 3 vertices" }
    }
    
    val triangleCount: Int get() = indices.size - 2
    
    fun toTriangles(vertices: List<Point3D>): List<Triangle3D> {
        if (indices.size == 3) {
            return listOf(Triangle3D(vertices[indices[0]], vertices[indices[1]], vertices[indices[2]]))
        }
        // Fan triangulation for quads and n-gons
        return (1 until indices.size - 1).map { i ->
            Triangle3D(vertices[indices[0]], vertices[indices[i]], vertices[indices[i + 1]])
        }
    }
    
    companion object {
        fun triangle(i0: Int, i1: Int, i2: Int) = Face(listOf(i0, i1, i2))
        fun quad(i0: Int, i1: Int, i2: Int, i3: Int) = Face(listOf(i0, i1, i2, i3))
    }
}

@Serializable
data class MeshMetadata(
    val name: String = "",
    val sourceFormat: String = "",
    val properties: Map<String, String> = emptyMap()
)
