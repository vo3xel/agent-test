package com.spatial.converter

import com.spatial.core.Vector3D
import kotlin.test.*

class VelocityToBearingTest {
    
    private val converter = VelocityToBearingConverter()
    
    @Test
    fun `north velocity gives 0 degree bearing`() {
        val velocity = Vector3D(0.0, 10.0, 0.0)  // 10 m/s North
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(0.0, result.bearing, 0.01)
        assertEquals(10.0, result.speed, 0.01)
        assertEquals("N", result.cardinalDirection)
    }
    
    @Test
    fun `east velocity gives 90 degree bearing`() {
        val velocity = Vector3D(10.0, 0.0, 0.0)  // 10 m/s East
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(90.0, result.bearing, 0.01)
        assertEquals(10.0, result.speed, 0.01)
        assertEquals("E", result.cardinalDirection)
    }
    
    @Test
    fun `south velocity gives 180 degree bearing`() {
        val velocity = Vector3D(0.0, -10.0, 0.0)  // 10 m/s South
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(180.0, result.bearing, 0.01)
        assertEquals("S", result.cardinalDirection)
    }
    
    @Test
    fun `west velocity gives 270 degree bearing`() {
        val velocity = Vector3D(-10.0, 0.0, 0.0)  // 10 m/s West
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(270.0, result.bearing, 0.01)
        assertEquals("W", result.cardinalDirection)
    }
    
    @Test
    fun `northeast velocity gives 45 degree bearing`() {
        val velocity = Vector3D(10.0, 10.0, 0.0)  // Equal East and North
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(45.0, result.bearing, 0.01)
        assertEquals("NE", result.cardinalDirection)
    }
    
    @Test
    fun `vertical component is ignored`() {
        val velocity = Vector3D(10.0, 0.0, 100.0)  // East with large vertical
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(90.0, result.bearing, 0.01)
        assertEquals(10.0, result.speed, 0.01)  // Only horizontal speed
    }
    
    @Test
    fun `stationary velocity returns zero bearing`() {
        val velocity = Vector3D(0.001, 0.001, 0.0)  // Below threshold
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(Bearing2D.STATIONARY, result)
    }
    
    @Test
    fun `speed is calculated correctly for diagonal movement`() {
        val velocity = Vector3D(3.0, 4.0, 0.0)  // 3-4-5 triangle
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(5.0, result.speed, 0.01)
    }
    
    @Test
    fun `batch conversion preserves order`() {
        val batchConverter = VelocitySequenceToBearingConverter()
        val velocities = listOf(
            Vector3D(0.0, 10.0, 0.0),   // North
            Vector3D(10.0, 0.0, 0.0),   // East
            Vector3D(0.0, -10.0, 0.0)   // South
        )
        
        val results = batchConverter.convert(velocities).getOrThrow()
        
        assertEquals(3, results.size)
        assertEquals(0.0, results[0].bearing, 0.01)
        assertEquals(90.0, results[1].bearing, 0.01)
        assertEquals(180.0, results[2].bearing, 0.01)
    }
    
    @Test
    fun `smoothed conversion reduces noise`() {
        val batchConverter = VelocitySequenceToBearingConverter()
        val velocities = listOf(
            Vector3D(0.0, 10.0, 0.0),   // North
            Vector3D(1.0, 10.0, 0.0),   // Slightly NE (noise)
            Vector3D(-1.0, 10.0, 0.0),  // Slightly NW (noise)
            Vector3D(0.0, 10.0, 0.0)    // North
        )
        
        val smoothed = batchConverter.convertSmoothed(velocities, windowSize = 3).getOrThrow()
        
        // Middle values should be closer to 0 (North) after smoothing
        assertTrue(smoothed[1].bearing < 5.0 || smoothed[1].bearing > 355.0)
    }
}
