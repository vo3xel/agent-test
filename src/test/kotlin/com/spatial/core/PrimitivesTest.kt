package com.spatial.core

import kotlin.test.*

class PrimitivesTest {
    
    @Test
    fun `vector addition is commutative`() {
        val a = Vector3D(1.0, 2.0, 3.0)
        val b = Vector3D(4.0, 5.0, 6.0)
        
        assertEquals(a + b, b + a)
    }
    
    @Test
    fun `vector normalization produces unit length`() {
        val v = Vector3D(3.0, 4.0, 0.0)
        val normalized = v.normalized()
        
        assertEquals(1.0, normalized.magnitude, 1e-10)
    }
    
    @Test
    fun `cross product of unit vectors`() {
        val result = Vector3D.UNIT_X.cross(Vector3D.UNIT_Y)
        
        assertEquals(Vector3D.UNIT_Z, result)
    }
    
    @Test
    fun `point subtraction yields vector`() {
        val a = Point3D(5.0, 5.0, 5.0)
        val b = Point3D(2.0, 3.0, 4.0)
        
        val diff = a - b
        
        assertEquals(Vector3D(3.0, 2.0, 1.0), diff)
    }
    
    @Test
    fun `bounding box contains interior point`() {
        val box = BoundingBox(
            min = Point3D(0.0, 0.0, 0.0),
            max = Point3D(10.0, 10.0, 10.0)
        )
        
        assertTrue(box.contains(Point3D(5.0, 5.0, 5.0)))
        assertFalse(box.contains(Point3D(15.0, 5.0, 5.0)))
    }
    
    @Test
    fun `bounding box from points`() {
        val points = listOf(
            Point3D(1.0, 2.0, 3.0),
            Point3D(-1.0, 5.0, 0.0),
            Point3D(4.0, -2.0, 8.0)
        )
        
        val box = BoundingBox.fromPoints(points)
        
        assertEquals(Point3D(-1.0, -2.0, 0.0), box.min)
        assertEquals(Point3D(4.0, 5.0, 8.0), box.max)
    }
    
    @Test
    fun `triangle normal calculation`() {
        val triangle = Triangle3D(
            Point3D(0.0, 0.0, 0.0),
            Point3D(1.0, 0.0, 0.0),
            Point3D(0.0, 1.0, 0.0)
        )
        
        val normal = triangle.normal
        
        // Triangle in XY plane should have Z-pointing normal
        assertEquals(0.0, normal.x, 1e-10)
        assertEquals(0.0, normal.y, 1e-10)
        assertEquals(1.0, kotlin.math.abs(normal.z), 1e-10)
    }
}
