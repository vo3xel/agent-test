package com.spatial.converter

import com.spatial.core.Vector3D
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Navigation-related conversions for velocity and bearing calculations.
 * Uses ENU (East-North-Up) coordinate convention common in geospatial applications.
 */

/**
 * Bearing result from velocity conversion.
 * @param bearing Clockwise angle from North in degrees [0, 360)
 * @param speed Horizontal speed magnitude in m/s (EN plane only)
 */
data class Bearing2D(
    val bearing: Double,
    val speed: Double
) {
    /**
     * Cardinal direction approximation (N, NE, E, SE, S, SW, W, NW)
     */
    val cardinalDirection: String get() = when {
        bearing < 22.5 || bearing >= 337.5 -> "N"
        bearing < 67.5 -> "NE"
        bearing < 112.5 -> "E"
        bearing < 157.5 -> "SE"
        bearing < 202.5 -> "S"
        bearing < 247.5 -> "SW"
        bearing < 292.5 -> "W"
        else -> "NW"
    }
    
    companion object {
        val STATIONARY = Bearing2D(0.0, 0.0)
    }
}

/**
 * Converts 3D velocity in ENU (East-North-Up) coordinates to 2D bearing.
 * 
 * ENU Convention:
 * - X = East (positive eastward)
 * - Y = North (positive northward)  
 * - Z = Up (positive upward, ignored for 2D bearing)
 *
 * Output bearing is measured clockwise from North (0° = North, 90° = East).
 * 
 * @param minimumSpeed Velocities below this threshold return STATIONARY (avoids noise)
 */
class VelocityToBearingConverter(
    private val minimumSpeed: Double = 0.01
) : Converter<Vector3D, Bearing2D> {
    
    override fun convert(input: Vector3D): Result<Bearing2D> = runCatching {
        val east = input.x
        val north = input.y
        // input.z (up) is ignored for 2D bearing
        
        val horizontalSpeed = sqrt(east * east + north * north)
        
        if (horizontalSpeed < minimumSpeed) {
            return@runCatching Bearing2D.STATIONARY
        }
        
        // atan2 gives angle from positive X axis (East), counter-clockwise
        // We need angle from positive Y axis (North), clockwise
        val angleFromEast = atan2(north, east)  // radians, CCW from East
        val angleFromNorth = Math.PI / 2 - angleFromEast  // Convert to CW from North
        
        // Normalize to [0, 360)
        var bearingDegrees = Math.toDegrees(angleFromNorth)
        if (bearingDegrees < 0) bearingDegrees += 360.0
        if (bearingDegrees >= 360.0) bearingDegrees -= 360.0
        
        Bearing2D(bearingDegrees, horizontalSpeed)
    }
}

/**
 * Batch converter for velocity sequences (e.g., trajectory data).
 */
class VelocitySequenceToBearingConverter(
    private val minimumSpeed: Double = 0.01
) {
    private val converter = VelocityToBearingConverter(minimumSpeed)
    
    fun convert(velocities: List<Vector3D>): Result<List<Bearing2D>> = runCatching {
        velocities.map { converter.convert(it).getOrThrow() }
    }
    
    /**
     * Convert with smoothing to reduce bearing jitter from noisy velocity data.
     * Uses simple moving average on velocity components before conversion.
     */
    fun convertSmoothed(velocities: List<Vector3D>, windowSize: Int = 3): Result<List<Bearing2D>> = runCatching {
        require(windowSize > 0) { "Window size must be positive" }
        
        if (velocities.size < windowSize) {
            return@runCatching velocities.map { converter.convert(it).getOrThrow() }
        }
        
        val smoothed = velocities.windowed(windowSize, 1, partialWindows = true) { window ->
            Vector3D(
                window.map { it.x }.average(),
                window.map { it.y }.average(),
                window.map { it.z }.average()
            )
        }
        
        smoothed.map { converter.convert(it).getOrThrow() }
    }
}
