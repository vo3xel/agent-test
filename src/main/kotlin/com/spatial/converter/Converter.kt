package com.spatial.converter

import com.spatial.core.*

/**
 * Base converter interface. Converters are stateless and reusable.
 * For streaming large files, use the Flow-based variants in StreamingConverter.
 */
interface Converter<I, O> {
    fun convert(input: I): Result<O>
}

/**
 * Format-specific reader/writer interfaces.
 * Implementations handle parsing and serialization details.
 */
interface FormatReader<T> {
    val supportedExtensions: Set<String>
    fun read(content: String): Result<T>
    fun canRead(extension: String): Boolean = extension.lowercase() in supportedExtensions
}

interface FormatWriter<T> {
    val supportedExtensions: Set<String>
    fun write(data: T): Result<String>
}

/**
 * Conversion result with error context for debugging.
 */
sealed class ConversionError(val message: String) {
    class ParseError(message: String, val line: Int? = null) : ConversionError(message)
    class ValidationError(message: String, val field: String? = null) : ConversionError(message)
    class UnsupportedFormat(val format: String) : ConversionError("Unsupported format: $format")
    class IoError(message: String, val cause: Throwable? = null) : ConversionError(message)
}

// Extension to convert ConversionError to Result
fun <T> ConversionError.toFailure(): Result<T> = Result.failure(
    ConversionException(message, this)
)

class ConversionException(
    message: String,
    val error: ConversionError
) : Exception(message)
