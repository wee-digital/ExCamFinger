package wee.vietinbank.kiosk.gl3

import android.opengl.GLES31
import java.nio.FloatBuffer

class PointVertex {

    companion object {

        private const val COORDINATES_PER_VERTEX = 3

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
    // this is the initial data, which will need to translated into the mVertices variable in the consturctor.
    private val verticesBuffer: FloatBuffer

    private var verticesData = facePoint

    private var programHandle: Int = 0

    private var mvpMatrixHandle: Int = 0

    private var colorHandle: Int = 0

    init {
        //first setup the mVertices correctly.
        verticesBuffer = newFloatBuffer(verticesData.size)
                .put(verticesData)
        verticesBuffer.position(0)
        initProgram()
    }

    /**
     * init program and copy vertices from cpu to the gpu
     */
    private fun initProgram() {

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

        GLES31.glLinkProgram(programHandle)
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

        val vertexCount = verticesData.size / COORDINATES_PER_VERTEX
        onDraw(vertexCount)
    }

    private fun onDraw(vertexCount: Int) {
        GLES31.glUniform4fv(colorHandle, 1, GL_WHITE, 0)
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, vertexCount)

        GLES31.glUniform3fv(colorHandle, 1, GL_WHITE, 0)
    }

}