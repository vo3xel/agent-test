package com.spatial.core

import kotlinx.serialization.Serializable

/**
 * Core 3D primitives using a right-handed coordinate system (Y-up).
 * All spatial types are immutable and support operator overloading.
 */

@Serializable
data class Point3D(
    val x: Double,
    val y: Double,
    val z: Double
) {
    operator fun plus(other: Vector3D) = Point3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    
    fun distanceTo(other: Point3D): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    companion object {
        val ORIGIN = Point3D(0.0, 0.0, 0.0)
    }
}

@Serializable
data class Vector3D(
    val x: Double,
    val y: Double,
    val z: Double
) {
    val magnitude: Double get() = kotlin.math.sqrt(x * x + y * y + z * z)
    
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)
    operator fun unaryMinus() = Vector3D(-x, -y, -z)
    
    fun normalized(): Vector3D {
        val mag = magnitude
        return if (mag > 0) Vector3D(x / mag, y / mag, z / mag) else this
    }
    
    fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z
    
    fun cross(other: Vector3D) = Vector3D(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
    
    companion object {
        val ZERO = Vector3D(0.0, 0.0, 0.0)
        val UNIT_X = Vector3D(1.0, 0.0, 0.0)
        val UNIT_Y = Vector3D(0.0, 1.0, 0.0)
        val UNIT_Z = Vector3D(0.0, 0.0, 1.0)
    }
}

@Serializable
data class Triangle3D(
    val v0: Point3D,
    val v1: Point3D,
    val v2: Point3D
) {
    val normal: Vector3D get() {
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        return edge1.cross(edge2).normalized()
    }
    
    val centroid: Point3D get() = Point3D(
        (v0.x + v1.x + v2.x) / 3.0,
        (v0.y + v1.y + v2.y) / 3.0,
        (v0.z + v1.z + v2.z) / 3.0
    )
}

@Serializable
data class BoundingBox(
    val min: Point3D,
    val max: Point3D
) {
    val center: Point3D get() = Point3D(
        (min.x + max.x) / 2.0,
        (min.y + max.y) / 2.0,
        (min.z + max.z) / 2.0
    )
    
    val dimensions: Vector3D get() = Vector3D(
        max.x - min.x,
        max.y - min.y,
        max.z - min.z
    )
    
    fun contains(point: Point3D): Boolean =
        point.x in min.x..max.x &&
        point.y in min.y..max.y &&
        point.z in min.z..max.z
    
    companion object {
        fun fromPoints(points: List<Point3D>): BoundingBox {
            require(points.isNotEmpty()) { "Cannot create bounding box from empty list" }
            return BoundingBox(
                min = Point3D(
                    points.minOf { it.x },
                    points.minOf { it.y },
                    points.minOf { it.z }
                ),
                max = Point3D(
                    points.maxOf { it.x },
                    points.maxOf { it.y },
                    points.maxOf { it.z }
                )
            )
        }
    }
}
