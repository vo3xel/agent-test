# Spatial Converter - AI Coding Instructions

## Project Overview
Kotlin library for converting 3D spatial data (meshes, point clouds) between formats. Uses Gradle with kotlinx.serialization and coroutines.

## Architecture

### Package Structure
- `core/` - Immutable 3D primitives (`Point3D`, `Vector3D`, `Mesh`, `PointCloud`)
- `converter/` - Stateless conversion logic implementing `Converter<I, O>` interface
- `format/` - File format readers/writers (`FormatReader<T>`, `FormatWriter<T>`)

### Key Design Patterns

**Immutable Data Classes**: All spatial types are immutable with `copy()` for modifications:
```kotlin
// ✓ Correct
val transformed = pointCloud.copy(points = newPoints)
// ✗ Wrong - don't try to mutate
```

**Result-Based Error Handling**: Converters return `Result<T>`, never throw:
```kotlin
fun convert(input: Mesh): Result<PointCloud> = runCatching { ... }
// Caller uses: result.getOrThrow() or result.getOrElse { }
```

**Operator Overloading**: Primitives support math operators:
```kotlin
val direction = pointB - pointA  // Returns Vector3D
val moved = point + vector       // Returns Point3D
```

## Coordinate Systems
Default is **right-handed Y-up** (OpenGL convention). Use `CoordinateTransformer` for conversions:
- `CoordinateTransformer.yUpToZUp()` - for CAD export
- `CoordinateTransformer.zUpToYUp()` - for CAD import

**ENU (East-North-Up)** convention for navigation/geospatial:
- X = East, Y = North, Z = Up
- Used by `VelocityToBearingConverter` for bearing calculations

## Adding New Formats

1. Create reader/writer in `format/` package
2. Implement `FormatReader<T>` or `FormatWriter<T>`
3. Declare `supportedExtensions: Set<String>`
4. Use `Result<T>` return type with `runCatching`

Example pattern from [ObjFormat.kt](src/main/kotlin/com/spatial/format/ObjFormat.kt):
```kotlin
class NewFormatReader : FormatReader<Mesh> {
    override val supportedExtensions = setOf("ext")
    override fun read(content: String): Result<Mesh> = runCatching { ... }
}
```

## Adding New Converters

1. Implement `Converter<Input, Output>` in `converter/` package
2. Use sealed classes for strategy variants (see `SamplingStrategy`)
3. Preserve metadata through conversions

## Build & Test
```bash
./gradlew build          # Build and test
./gradlew test           # Run tests only
./gradlew run            # Run demo
```

## Testing Conventions
- Test file mirrors source: `core/Mesh.kt` → `core/MeshTest.kt`
- Use backtick test names: `` @Test fun `descriptive behavior`() ``
- Test roundtrips for format readers/writers
- Use tolerance for floating-point: `assertEquals(expected, actual, 1e-10)`

## Common Tasks

**Parse OBJ file**: `ObjReader().read(content).getOrThrow()`

**Mesh → Point Cloud**: 
```kotlin
MeshToPointCloudConverter(SamplingStrategy.FixedCount(1000)).convert(mesh)
```

**Transform coordinates**: `CoordinateTransformer.yUpToZUp().convert(cloud)`

**3D ENU velocity → 2D bearing**:
```kotlin
val velocity = Vector3D(east = 5.0, north = 8.66, up = 0.0)  // m/s ENU
val bearing = VelocityToBearingConverter().convert(velocity).getOrThrow()
// bearing.bearing = 30.0 (degrees CW from North)
// bearing.speed = 10.0 (horizontal m/s, ignores Up component)
// bearing.cardinalDirection = "NE"
```
