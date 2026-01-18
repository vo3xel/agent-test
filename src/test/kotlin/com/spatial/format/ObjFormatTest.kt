package com.spatial.format

import com.spatial.core.*
import kotlin.test.*

class ObjFormatTest {
    
    private val reader = ObjReader()
    private val writer = ObjWriter()
    
    @Test
    fun `read simple triangle`() {
        val obj = """
            # Simple triangle
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
        """.trimIndent()
        
        val result = reader.read(obj)
        
        assertTrue(result.isSuccess)
        val mesh = result.getOrThrow()
        assertEquals(3, mesh.vertices.size)
        assertEquals(1, mesh.faces.size)
        assertEquals(listOf(0, 1, 2), mesh.faces[0].indices)
    }
    
    @Test
    fun `read face with texture and normal indices`() {
        val obj = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            vn 0.0 0.0 1.0
            f 1/1/1 2/2/1 3/3/1
        """.trimIndent()
        
        val mesh = reader.read(obj).getOrThrow()
        
        // Should extract vertex indices, ignoring texture/normal
        assertEquals(listOf(0, 1, 2), mesh.faces[0].indices)
    }
    
    @Test
    fun `read object name`() {
        val obj = """
            o MyCube
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
        """.trimIndent()
        
        val mesh = reader.read(obj).getOrThrow()
        
        assertEquals("MyCube", mesh.metadata.name)
    }
    
    @Test
    fun `write and read roundtrip`() {
        val original = Mesh(
            vertices = listOf(
                Point3D(0.0, 0.0, 0.0),
                Point3D(1.0, 0.0, 0.0),
                Point3D(0.0, 1.0, 0.0),
                Point3D(1.0, 1.0, 0.0)
            ),
            faces = listOf(
                Face.triangle(0, 1, 2),
                Face.triangle(1, 3, 2)
            ),
            metadata = MeshMetadata(name = "quad")
        )
        
        val written = writer.write(original).getOrThrow()
        val parsed = reader.read(written).getOrThrow()
        
        assertEquals(original.vertices.size, parsed.vertices.size)
        assertEquals(original.faces.size, parsed.faces.size)
        
        // Verify vertex positions match
        for (i in original.vertices.indices) {
            assertEquals(original.vertices[i].x, parsed.vertices[i].x, 1e-10)
            assertEquals(original.vertices[i].y, parsed.vertices[i].y, 1e-10)
            assertEquals(original.vertices[i].z, parsed.vertices[i].z, 1e-10)
        }
    }
}
