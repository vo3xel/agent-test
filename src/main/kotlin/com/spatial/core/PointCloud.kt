package com.spatial.core

import kotlinx.serialization.Serializable

/**
 * Point cloud representation with optional per-point attributes.
 * Designed for large datasets - use sequences for processing.
 */
@Serializable
data class PointCloud(
    val points: List<Point3D>,
    val colors: List<Color>? = null,
    val normals: List<Vector3D>? = null,
    val intensity: List<Double>? = null,
    val metadata: PointCloudMetadata = PointCloudMetadata()
) {
    val size: Int get() = points.size
    val boundingBox: BoundingBox by lazy { BoundingBox.fromPoints(points) }
    
    init {
        colors?.let { require(it.size == points.size) { "Colors must match point count" } }
        normals?.let { require(it.size == points.size) { "Normals must match point count" } }
        intensity?.let { require(it.size == points.size) { "Intensity must match point count" } }
    }
    
    fun withColors(colors: List<Color>) = copy(colors = colors)
    fun withNormals(normals: List<Vector3D>) = copy(normals = normals)
    
    /**
     * Downsample using voxel grid filtering.
     * @param voxelSize Size of each voxel cube
     */
    fun voxelDownsample(voxelSize: Double): PointCloud {
        val voxelMap = mutableMapOf<Triple<Int, Int, Int>, MutableList<Int>>()
        
        for ((index, point) in points.withIndex()) {
            val key = Triple(
                (point.x / voxelSize).toInt(),
                (point.y / voxelSize).toInt(),
                (point.z / voxelSize).toInt()
            )
            voxelMap.getOrPut(key) { mutableListOf() }.add(index)
        }
        
        val newPoints = voxelMap.values.map { indices ->
            val avgX = indices.map { points[it].x }.average()
            val avgY = indices.map { points[it].y }.average()
            val avgZ = indices.map { points[it].z }.average()
            Point3D(avgX, avgY, avgZ)
        }
        
        return PointCloud(newPoints, metadata = metadata.copy(sourceFormat = "downsampled"))
    }
}

@Serializable
data class Color(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255
) {
    init {
        require(r in 0..255 && g in 0..255 && b in 0..255 && a in 0..255) {
            "Color components must be in range 0-255"
        }
    }
    
    fun toHex(): String = "#%02X%02X%02X".format(r, g, b)
    
    companion object {
        val WHITE = Color(255, 255, 255)
        val BLACK = Color(0, 0, 0)
        val RED = Color(255, 0, 0)
        val GREEN = Color(0, 255, 0)
        val BLUE = Color(0, 0, 255)
        
        fun fromHex(hex: String): Color {
            val clean = hex.removePrefix("#")
            require(clean.length == 6) { "Hex color must be 6 characters" }
            return Color(
                clean.substring(0, 2).toInt(16),
                clean.substring(2, 4).toInt(16),
                clean.substring(4, 6).toInt(16)
            )
        }
    }
}

@Serializable
data class PointCloudMetadata(
    val name: String = "",
    val sourceFormat: String = "",
    val coordinateSystem: CoordinateSystem = CoordinateSystem.RIGHT_HANDED_Y_UP,
    val properties: Map<String, String> = emptyMap()
)

enum class CoordinateSystem {
    RIGHT_HANDED_Y_UP,   // OpenGL, Blender default
    RIGHT_HANDED_Z_UP,   // CAD systems, some game engines
    LEFT_HANDED_Y_UP     // DirectX, Unity
}
