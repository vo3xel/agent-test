package com.spatial.converter

import com.spatial.core.*
import kotlin.test.*

class MeshToPointCloudTest {
    
    private val simpleMesh = Mesh(
        vertices = listOf(
            Point3D(0.0, 0.0, 0.0),
            Point3D(1.0, 0.0, 0.0),
            Point3D(0.0, 1.0, 0.0)
        ),
        faces = listOf(Face.triangle(0, 1, 2))
    )
    
    @Test
    fun `vertices only strategy returns mesh vertices`() {
        val converter = MeshToPointCloudConverter(SamplingStrategy.VerticesOnly)
        
        val result = converter.convert(simpleMesh)
        
        assertTrue(result.isSuccess)
        val cloud = result.getOrThrow()
        assertEquals(3, cloud.size)
        assertEquals(simpleMesh.vertices, cloud.points)
    }
    
    @Test
    fun `fixed count strategy returns approximate count`() {
        val converter = MeshToPointCloudConverter(SamplingStrategy.FixedCount(100))
        
        val result = converter.convert(simpleMesh)
        
        assertTrue(result.isSuccess)
        val cloud = result.getOrThrow()
        assertTrue(cloud.size in 90..110, "Expected ~100 points, got ${cloud.size}")
    }
    
    @Test
    fun `sampled points lie on mesh surface`() {
        val converter = MeshToPointCloudConverter(SamplingStrategy.FixedCount(50))
        val cloud = converter.convert(simpleMesh).getOrThrow()
        
        // All points should be in the XY plane (z=0) for this triangle
        for (point in cloud.points) {
            assertEquals(0.0, point.z, 1e-10, "Point should be on triangle surface")
            assertTrue(point.x >= 0.0 && point.y >= 0.0, "Point should be in positive quadrant")
            assertTrue(point.x + point.y <= 1.0 + 1e-10, "Point should be within triangle")
        }
    }
    
    @Test
    fun `metadata preserves source info`() {
        val meshWithName = simpleMesh.copy(
            metadata = MeshMetadata(name = "test-mesh")
        )
        val converter = MeshToPointCloudConverter()
        
        val cloud = converter.convert(meshWithName).getOrThrow()
        
        assertEquals("test-mesh", cloud.metadata.name)
        assertEquals("mesh", cloud.metadata.sourceFormat)
    }
}
