package com.spatial

import com.spatial.converter.*
import com.spatial.core.*
import com.spatial.format.*

fun main(args: Array<String>) {
    println("Spatial Converter - 3D Data Conversion Tool")
    println("============================================")
    
    // Demo: Create a simple mesh (cube)
    val cube = createCube()
    println("\nCreated cube mesh:")
    println("  Vertices: ${cube.vertices.size}")
    println("  Faces: ${cube.faces.size}")
    println("  Bounding box: ${cube.boundingBox}")
    
    // Convert mesh to point cloud
    val converter = MeshToPointCloudConverter(SamplingStrategy.FixedCount(1000))
    val pointCloud = converter.convert(cube).getOrThrow()
    println("\nConverted to point cloud:")
    println("  Points: ${pointCloud.size}")
    
    // Transform coordinate system (Y-up to Z-up)
    val transformer = CoordinateTransformer.yUpToZUp()
    val transformed = transformer.convert(pointCloud).getOrThrow()
    println("\nTransformed to Z-up coordinate system")
    
    // Export to different formats
    val objWriter = ObjWriter()
    val plyWriter = PlyWriter()
    val jsonWriter = JsonPointCloudWriter(prettyPrint = true)
    
    println("\nExport formats available:")
    println("  OBJ: ${objWriter.write(cube).isSuccess}")
    println("  PLY: ${plyWriter.write(transformed).isSuccess}")
    println("  JSON: ${jsonWriter.write(transformed).isSuccess}")
}

private fun createCube(): Mesh {
    val vertices = listOf(
        Point3D(-1.0, -1.0, -1.0),
        Point3D( 1.0, -1.0, -1.0),
        Point3D( 1.0,  1.0, -1.0),
        Point3D(-1.0,  1.0, -1.0),
        Point3D(-1.0, -1.0,  1.0),
        Point3D( 1.0, -1.0,  1.0),
        Point3D( 1.0,  1.0,  1.0),
        Point3D(-1.0,  1.0,  1.0)
    )
    
    val faces = listOf(
        Face.quad(0, 1, 2, 3),  // Front
        Face.quad(5, 4, 7, 6),  // Back
        Face.quad(4, 0, 3, 7),  // Left
        Face.quad(1, 5, 6, 2),  // Right
        Face.quad(3, 2, 6, 7),  // Top
        Face.quad(4, 5, 1, 0)   // Bottom
    )
    
    return Mesh(vertices, faces, metadata = MeshMetadata(name = "cube"))
}
