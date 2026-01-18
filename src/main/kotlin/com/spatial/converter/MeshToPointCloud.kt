package com.spatial.converter

import com.spatial.core.*

/**
 * Converts mesh to point cloud by sampling points on triangle surfaces.
 * Uses barycentric coordinates for uniform distribution.
 */
class MeshToPointCloudConverter(
    private val samplingStrategy: SamplingStrategy = SamplingStrategy.VerticesOnly
) : Converter<Mesh, PointCloud> {
    
    override fun convert(input: Mesh): Result<PointCloud> = runCatching {
        val points = when (samplingStrategy) {
            is SamplingStrategy.VerticesOnly -> input.vertices
            is SamplingStrategy.UniformSurface -> sampleSurface(input, samplingStrategy.pointsPerUnit)
            is SamplingStrategy.FixedCount -> sampleFixedCount(input, samplingStrategy.totalPoints)
        }
        
        PointCloud(
            points = points,
            metadata = PointCloudMetadata(
                name = input.metadata.name,
                sourceFormat = "mesh",
                properties = mapOf("original_vertices" to input.vertices.size.toString())
            )
        )
    }
    
    private fun sampleSurface(mesh: Mesh, pointsPerUnit: Double): List<Point3D> {
        val points = mutableListOf<Point3D>()
        
        for (triangle in mesh.triangles()) {
            val area = triangleArea(triangle)
            val sampleCount = (area * pointsPerUnit).toInt().coerceAtLeast(1)
            
            repeat(sampleCount) {
                points.add(randomPointInTriangle(triangle))
            }
        }
        
        return points
    }
    
    private fun sampleFixedCount(mesh: Mesh, totalPoints: Int): List<Point3D> {
        val triangles = mesh.triangles().toList()
        if (triangles.isEmpty()) return emptyList()
        
        // Weight triangles by area for uniform distribution
        val areas = triangles.map { triangleArea(it) }
        val totalArea = areas.sum()
        
        val points = mutableListOf<Point3D>()
        for ((index, triangle) in triangles.withIndex()) {
            val weight = areas[index] / totalArea
            val sampleCount = (totalPoints * weight).toInt().coerceAtLeast(1)
            repeat(sampleCount) {
                points.add(randomPointInTriangle(triangle))
            }
        }
        
        return points.take(totalPoints)
    }
    
    private fun triangleArea(t: Triangle3D): Double {
        val ab = t.v1 - t.v0
        val ac = t.v2 - t.v0
        return ab.cross(ac).magnitude / 2.0
    }
    
    private fun randomPointInTriangle(t: Triangle3D): Point3D {
        // Barycentric coordinates for uniform sampling
        var r1 = Math.random()
        var r2 = Math.random()
        if (r1 + r2 > 1) {
            r1 = 1 - r1
            r2 = 1 - r2
        }
        val r3 = 1 - r1 - r2
        
        return Point3D(
            r1 * t.v0.x + r2 * t.v1.x + r3 * t.v2.x,
            r1 * t.v0.y + r2 * t.v1.y + r3 * t.v2.y,
            r1 * t.v0.z + r2 * t.v1.z + r3 * t.v2.z
        )
    }
}

sealed class SamplingStrategy {
    object VerticesOnly : SamplingStrategy()
    data class UniformSurface(val pointsPerUnit: Double = 100.0) : SamplingStrategy()
    data class FixedCount(val totalPoints: Int) : SamplingStrategy()
}
