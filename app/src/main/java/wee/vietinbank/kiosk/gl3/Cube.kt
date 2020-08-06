package wee.vietinbank.kiosk.gl3

import android.opengl.GLES31
import java.nio.FloatBuffer

class Cube {

    companion object {

        private const val VERTICES_PER_CUBE_FACE = 6

        private const val VERTEX_SHADER_CODE = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            void main() {
               gl_Position = uMVPMatrix * vPosition;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            uniform vec4 vColor;
            out vec4 fragColor;
            void main() {
              fragColor = vColor;
            }
        """

    }

    var size = 0.5f

    private val verticesBuffer: FloatBuffer

    private var verticesData = floatArrayOf( ////////////////////////////////////////////////////////////////////
            // FRONT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, size,  // top-left
            -size, -size, size,  // bottom-left
            size, -size, size,  // bottom-right
            // Triangle 2
            size, -size, size,  // bottom-right
            size, size, size,  // top-right
            -size, size, size,  // top-left
            ////////////////////////////////////////////////////////////////////
            // BACK
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size,  // top-left
            -size, -size, -size,  // bottom-left
            size, -size, -size,  // bottom-right
            // Triangle 2
            size, -size, -size,  // bottom-right
            size, size, -size,  // top-right
            -size, size, -size,  // top-left
            ////////////////////////////////////////////////////////////////////
            // LEFT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size,  // top-left
            -size, -size, -size,  // bottom-left
            -size, -size, size,  // bottom-right
            // Triangle 2
            -size, -size, size,  // bottom-right
            -size, size, size,  // top-right
            -size, size, -size,  // top-left
            ////////////////////////////////////////////////////////////////////
            // RIGHT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            size, size, -size,  // top-left
            size, -size, -size,  // bottom-left
            size, -size, size,  // bottom-right
            // Triangle 2
            size, -size, size,  // bottom-right
            size, size, size,  // top-right
            size, size, -size,  // top-left
            ////////////////////////////////////////////////////////////////////
            // TOP
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size,  // top-left
            -size, size, size,  // bottom-left
            size, size, size,  // bottom-right
            // Triangle 2
            size, size, size,  // bottom-right
            size, size, -size,  // top-right
            -size, size, -size,  // top-left
            ////////////////////////////////////////////////////////////////////
            // BOTTOM
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, -size, -size,  // top-left
            -size, -size, size,  // bottom-left
            size, -size, size,  // bottom-right
            // Triangle 2
            size, -size, size,  // bottom-right
            size, -size, -size,  // top-right
            -size, -size, -size // top-left
    )

    private var programHandle: Int = 0

    private var mvpMatrixHandle: Int = 0

    private var colorHandle: Int = 0

    init {
        //first setup the vertices.
        verticesBuffer = newFloatBuffer(verticesData.size)
                .put(verticesData)
        verticesBuffer.position(0)
        initProgram()
    }

    /**
     * init program and copy vertices from cpu to the gpu
     */
    private fun initProgram() {
        // Create the program object
        programHandle = GLES31.glCreateProgram()
        if (programHandle == 0) {
            println("some kind of error")
            return
        }

        // Load the vertex/fragment shader
        val vertexShader: Int = loadShader(GLES31.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader: Int = loadShader(GLES31.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        GLES31.glAttachShader(programHandle, vertexShader)
        GLES31.glAttachShader(programHandle, fragmentShader)

        // Bind vPosition to attribute 0
        GLES31.glBindAttribLocation(programHandle, 0, "vPosition")

        // Link the program
        GLES31.glLinkProgram(programHandle)

        checkLinkedProgram()
    }

    private fun checkLinkedProgram() {
        val linked = IntArray(1)
        GLES31.glGetProgramiv(programHandle, GLES31.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            println(GLES31.glGetProgramInfoLog(programHandle))
            GLES31.glDeleteProgram(programHandle)
            println("everything is setup failed")
        } else {
            println("everything is setup and ready to draw.")
        }
    }

    fun draw(mvpMatrix: FloatArray?) {

        // Use the program object
        GLES31.glUseProgram(programHandle)

        // get handle to shape's transformation matrix
        mvpMatrixHandle = GLES31.glGetUniformLocation(programHandle, "uMVPMatrix")
        checkGlError("glGetUniformLocation")

        // get handle to fragment shader's vColor member
        colorHandle = GLES31.glGetUniformLocation(programHandle, "vColor")

        // Apply the projection and view transformation
        GLES31.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")

        // Enable vertex
        val vertexPosIndex = 0
        verticesBuffer.position(vertexPosIndex) //just in case.  We did it already though.
        //add all the points to the space, so they can be correct by the transformations.
        //would need to do this even if there were no transformations actually.
        GLES31.glVertexAttribPointer(
                vertexPosIndex,
                3,
                GLES31.GL_FLOAT,
                false,
                0,
                verticesBuffer
        )
        GLES31.glEnableVertexAttribArray(vertexPosIndex)

        onDraw()
    }

    private fun onDraw() {
        var startPos = 0

        //draw front face
        GLES31.glUniform4fv(colorHandle, 1, GL_BLUE, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        startPos += VERTICES_PER_CUBE_FACE

        //draw back face
        GLES31.glUniform4fv(colorHandle, 1, GL_CYAN, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        startPos += VERTICES_PER_CUBE_FACE

        //draw left face
        GLES31.glUniform4fv(colorHandle, 1, GL_RED, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        startPos += VERTICES_PER_CUBE_FACE

        //draw right face
        GLES31.glUniform4fv(colorHandle, 1, GL_GRAY, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        startPos += VERTICES_PER_CUBE_FACE

        //draw top face
        GLES31.glUniform4fv(colorHandle, 1, GL_GREEN, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        startPos += VERTICES_PER_CUBE_FACE

        //draw bottom face
        GLES31.glUniform4fv(colorHandle, 1, GL_YELLOW, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, VERTICES_PER_CUBE_FACE)
        //last face, so no need to increment.
    }

}