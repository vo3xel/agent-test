# Spatial Converter

Kotlin library for converting 3D spatial data between formats.

## Features

- **Core primitives**: `Point3D`, `Vector3D`, `Triangle3D`, `BoundingBox`
- **Data structures**: `Mesh` (indexed vertices + faces), `PointCloud` (with colors, normals)
- **Format support**: OBJ, PLY, JSON
- **Transformations**: Coordinate system conversion, scaling, translation

## Quick Start

```bash
# Build
./gradlew build

# Run demo
./gradlew run

# Run tests
./gradlew test
```

## Usage

```kotlin
// Read OBJ file
val mesh = ObjReader().read(objContent).getOrThrow()

// Convert to point cloud
val converter = MeshToPointCloudConverter(SamplingStrategy.FixedCount(1000))
val cloud = converter.convert(mesh).getOrThrow()

// Transform coordinate system (Y-up to Z-up)
val transformed = CoordinateTransformer.yUpToZUp().convert(cloud).getOrThrow()

// Export to PLY
val plyContent = PlyWriter().write(transformed).getOrThrow()
```

## Project Structure

```
src/main/kotlin/com/spatial/
├── core/           # Immutable 3D primitives
├── converter/      # Data conversion logic
└── format/         # File format readers/writers
```

## Requirements

- JDK 17+
- Gradle 8.5+ (wrapper included)
