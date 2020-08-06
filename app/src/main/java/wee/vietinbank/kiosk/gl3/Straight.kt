package wee.vietinbank.kiosk.gl3

import android.opengl.GLES20
import android.opengl.GLES31
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Straight {

    companion object {

        private const val VERTICES_PER_POINT = 3

        private const val VERTEX_SHADER_CODE = """
            attribute vec4 vPosition;
            void main(void) {
                gl_Position = vPosition;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            void main() {
                gl_FragColor = vec4(1, 1, 1, 1);
            }
        """

    }

    var size = 0.4f

    // this is the initial data, which will need to translated into the mVertices variable in the consturctor.
    private val verticesBuffer: FloatBuffer

    private var verticesData = pyramidPoints

    private var programHandle: Int = 0

    private var vertexBufferId: Int = 0

    private var vertexCount: Int = 0

    private var vertexStride: Int = 0

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

        // Link the program
        GLES31.glLinkProgram(programHandle)

        val buffer = IntBuffer.allocate(1)
        GLES31.glGenBuffers(1, buffer)
        vertexBufferId = buffer[0]
        GLES31.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES31.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesData.size * 4, verticesBuffer, GLES31.GL_STATIC_DRAW)
        vertexCount = verticesData.size / VERTICES_PER_POINT
        vertexStride = VERTICES_PER_POINT * 4 // 4 bytes per vertex
    }

    fun draw(mvpMatrix: FloatArray?) {

        // Use the program object
        GLES31.glUseProgram(programHandle)

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


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount)
    }

}