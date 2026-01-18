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
    
    @Test
    fun `southeast velocity gives 135 degree bearing`() {
        val velocity = Vector3D(10.0, -10.0, 0.0)  // Equal East and South
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(135.0, result.bearing, 0.01)
        assertEquals("SE", result.cardinalDirection)
    }
    
    @Test
    fun `southwest velocity gives 225 degree bearing`() {
        val velocity = Vector3D(-10.0, -10.0, 0.0)  // Equal West and South
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(225.0, result.bearing, 0.01)
        assertEquals("SW", result.cardinalDirection)
    }
    
    @Test
    fun `northwest velocity gives 315 degree bearing`() {
        val velocity = Vector3D(-10.0, 10.0, 0.0)  // Equal West and North
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(315.0, result.bearing, 0.01)
        assertEquals("NW", result.cardinalDirection)
    }
    
    @Test
    fun `bearing near 360 degrees wraps correctly`() {
        val velocity = Vector3D(0.1, 10.0, 0.0)  // Slightly east of north
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertTrue(result.bearing >= 0.0 && result.bearing < 360.0)
        assertTrue(result.bearing < 10.0)  // Should be close to 0
        assertEquals("N", result.cardinalDirection)
    }
    
    @Test
    fun `bearing near 0 degrees is calculated correctly`() {
        val velocity = Vector3D(-0.1, 10.0, 0.0)  // Slightly west of north
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertTrue(result.bearing >= 0.0 && result.bearing < 360.0)
        assertTrue(result.bearing > 350.0 || result.bearing < 10.0)
        assertEquals("N", result.cardinalDirection)
    }
    
    @Test
    fun `very large velocity maintains precision`() {
        val velocity = Vector3D(1000.0, 1000.0, 0.0)  // Very large NE velocity
        
        val result = converter.convert(velocity).getOrThrow()
        
        assertEquals(45.0, result.bearing, 0.01)
        assertEquals("NE", result.cardinalDirection)
        assertTrue(result.speed > 1400.0)  // sqrt(1000^2 + 1000^2)
    }
    
    @Test
    fun `cardinal direction boundaries are correct`() {
        // Test boundaries between cardinal directions
        val testCases = listOf(
            Vector3D(1.0, 10.0, 0.0) to "N",   // 5.7° - North
            Vector3D(5.0, 5.0, 0.0) to "NE",   // 45° - Northeast
            Vector3D(10.0, 1.0, 0.0) to "E",   // 84.3° - East
            Vector3D(10.0, -5.0, 0.0) to "SE", // 116.6° - Southeast
            Vector3D(1.0, -10.0, 0.0) to "S",  // 174.3° - South
            Vector3D(-5.0, -5.0, 0.0) to "SW", // 225° - Southwest
            Vector3D(-10.0, -1.0, 0.0) to "W", // 264.3° - West
            Vector3D(-10.0, 5.0, 0.0) to "NW"  // 296.6° - Northwest
        )
        
        for ((velocity, expectedDirection) in testCases) {
            val result = converter.convert(velocity).getOrThrow()
            assertEquals(expectedDirection, result.cardinalDirection, 
                "Velocity $velocity should be $expectedDirection")
        }
    }
    
    @Test
    fun `ENU coordinate system is respected`() {
        // Verify that X=East, Y=North, Z=Up convention is correctly followed
        val eastVelocity = Vector3D(5.0, 0.0, 0.0)
        val northVelocity = Vector3D(0.0, 5.0, 0.0)
        val upVelocity = Vector3D(0.0, 0.0, 5.0)
        
        val eastResult = converter.convert(eastVelocity).getOrThrow()
        val northResult = converter.convert(northVelocity).getOrThrow()
        val upResult = converter.convert(upVelocity).getOrThrow()
        
        assertEquals(90.0, eastResult.bearing, 0.01, "X-axis should be East (90°)")
        assertEquals(0.0, northResult.bearing, 0.01, "Y-axis should be North (0°)")
        assertEquals(Bearing2D.STATIONARY, upResult, "Z-axis (Up) should result in stationary")
    }
    
    @Test
    fun `precision test for various angles`() {
        // Test precise angle calculations at various known points
        val testCases = listOf(
            Vector3D(5.0, 8.66, 0.0) to 30.0,    // 30° from North
            Vector3D(8.66, 5.0, 0.0) to 60.0,    // 60° from North
            Vector3D(8.66, -5.0, 0.0) to 120.0,  // 120° from North
            Vector3D(5.0, -8.66, 0.0) to 150.0,  // 150° from North
            Vector3D(-5.0, -8.66, 0.0) to 210.0, // 210° from North
            Vector3D(-8.66, -5.0, 0.0) to 240.0, // 240° from North
            Vector3D(-5.0, 8.66, 0.0) to 330.0   // 330° from North
        )
        
        for ((velocity, expectedBearing) in testCases) {
            val result = converter.convert(velocity).getOrThrow()
            assertEquals(expectedBearing, result.bearing, 1.0, 
                "Velocity $velocity should have bearing ~$expectedBearing°")
        }
    }
}
