package com.spatial.converter

import com.spatial.core.*

/**
 * Transforms spatial data between coordinate systems.
 * Common transforms: Y-up ↔ Z-up, scale, rotation, translation.
 */
class CoordinateTransformer(
    private val transform: Transform3D
) : Converter<PointCloud, PointCloud> {
    
    override fun convert(input: PointCloud): Result<PointCloud> = runCatching {
        input.copy(
            points = input.points.map { transform.apply(it) },
            normals = input.normals?.map { transform.applyToVector(it) }
        )
    }
    
    companion object {
        /**
         * Convert from Y-up to Z-up coordinate system (90° rotation around X).
         */
        fun yUpToZUp() = CoordinateTransformer(
            Transform3D.rotationX(-Math.PI / 2)
        )
        
        /**
         * Convert from Z-up to Y-up coordinate system.
         */
        fun zUpToYUp() = CoordinateTransformer(
            Transform3D.rotationX(Math.PI / 2)
        )
        
        fun scale(factor: Double) = CoordinateTransformer(
            Transform3D.scale(factor, factor, factor)
        )
        
        fun translate(offset: Vector3D) = CoordinateTransformer(
            Transform3D.translation(offset.x, offset.y, offset.z)
        )
    }
}

/**
 * 4x4 transformation matrix for 3D transforms.
 * Stored as row-major array of 16 doubles.
 */
data class Transform3D(private val matrix: DoubleArray) {
    init {
        require(matrix.size == 16) { "Transform matrix must have 16 elements" }
    }
    
    fun apply(point: Point3D): Point3D {
        val x = matrix[0] * point.x + matrix[1] * point.y + matrix[2] * point.z + matrix[3]
        val y = matrix[4] * point.x + matrix[5] * point.y + matrix[6] * point.z + matrix[7]
        val z = matrix[8] * point.x + matrix[9] * point.y + matrix[10] * point.z + matrix[11]
        return Point3D(x, y, z)
    }
    
    fun applyToVector(v: Vector3D): Vector3D {
        // Vectors ignore translation (w=0)
        val x = matrix[0] * v.x + matrix[1] * v.y + matrix[2] * v.z
        val y = matrix[4] * v.x + matrix[5] * v.y + matrix[6] * v.z
        val z = matrix[8] * v.x + matrix[9] * v.y + matrix[10] * v.z
        return Vector3D(x, y, z)
    }
    
    fun then(other: Transform3D): Transform3D {
        val result = DoubleArray(16)
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0.0
                for (k in 0..3) {
                    sum += this.matrix[row * 4 + k] * other.matrix[k * 4 + col]
                }
                result[row * 4 + col] = sum
            }
        }
        return Transform3D(result)
    }
    
    companion object {
        val IDENTITY = Transform3D(doubleArrayOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0
        ))
        
        fun translation(tx: Double, ty: Double, tz: Double) = Transform3D(doubleArrayOf(
            1.0, 0.0, 0.0, tx,
            0.0, 1.0, 0.0, ty,
            0.0, 0.0, 1.0, tz,
            0.0, 0.0, 0.0, 1.0
        ))
        
        fun scale(sx: Double, sy: Double, sz: Double) = Transform3D(doubleArrayOf(
            sx,  0.0, 0.0, 0.0,
            0.0, sy,  0.0, 0.0,
            0.0, 0.0, sz,  0.0,
            0.0, 0.0, 0.0, 1.0
        ))
        
        fun rotationX(radians: Double): Transform3D {
            val c = kotlin.math.cos(radians)
            val s = kotlin.math.sin(radians)
            return Transform3D(doubleArrayOf(
                1.0, 0.0, 0.0, 0.0,
                0.0, c,   -s,  0.0,
                0.0, s,    c,  0.0,
                0.0, 0.0, 0.0, 1.0
            ))
        }
        
        fun rotationY(radians: Double): Transform3D {
            val c = kotlin.math.cos(radians)
            val s = kotlin.math.sin(radians)
            return Transform3D(doubleArrayOf(
                c,   0.0, s,   0.0,
                0.0, 1.0, 0.0, 0.0,
                -s,  0.0, c,   0.0,
                0.0, 0.0, 0.0, 1.0
            ))
        }
        
        fun rotationZ(radians: Double): Transform3D {
            val c = kotlin.math.cos(radians)
            val s = kotlin.math.sin(radians)
            return Transform3D(doubleArrayOf(
                c,   -s,  0.0, 0.0,
                s,    c,  0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
            ))
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transform3D) return false
        return matrix.contentEquals(other.matrix)
    }
    
    override fun hashCode(): Int = matrix.contentHashCode()
}
