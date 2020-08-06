package wee.vietinbank.kiosk.gl3

import android.opengl.GLES31
import android.util.Log
import wee.vietinbank.kiosk.gl3.OverlayRenderer.Companion.loadShader
import wee.vietinbank.kiosk.gl3.OverlayRenderer.Companion.checkGlError
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube {

    companion object {

        private const val TAG = "Cube"

        private var VERTEX_SHADER_CODE = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            void main()
            {
               gl_Position = uMVPMatrix * vPosition;
            }
        """

        private var FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            uniform vec4 vColor;
            out vec4 fragColor;
            void main() {
              fragColor = vColor;
            }
        """

    }

    var size = 0.4f

    // this is the initial data, which will need to translated into the mVertices variable in the consturctor.
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
    private val vertices: FloatBuffer

    init {
        //first setup the mVertices correctly.
        vertices = ByteBuffer
                .allocateDirect(verticesData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(verticesData)
        vertices.position(0)

        //setup the shaders
        val linked = IntArray(1)

        // Load the vertex/fragment shaders
        val vertexShader: Int = loadShader(GLES31.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader: Int = loadShader(GLES31.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        // Create the program object
        val iProgram: Int = GLES31.glCreateProgram()
        if (iProgram == 0) {
            Log.i(TAG, "some kind of error")
        } else {
            GLES31.glAttachShader(iProgram, vertexShader)
            GLES31.glAttachShader(iProgram, fragmentShader)

            // Bind vPosition to attribute 0
            GLES31.glBindAttribLocation(iProgram, 0, "vPosition")

            // Link the program
            GLES31.glLinkProgram(iProgram)

            // Check the link status
            GLES31.glGetProgramiv(iProgram, GLES31.GL_LINK_STATUS, linked, 0)
            if (linked[0] == 0) {
                Log.i(TAG, GLES31.glGetProgramInfoLog(iProgram))
                GLES31.glDeleteProgram(iProgram)
            } else {
                Log.i(TAG, " everything is setup and ready to draw.")
                programHandle = iProgram
            }
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
        val VERTEX_POS_INDX = 0
        vertices.position(VERTEX_POS_INDX) //just in case.  We did it already though.

        //add all the points to the space, so they can be correct by the transformations.
        //would need to do this even if there were no transformations actually.
        GLES31.glVertexAttribPointer(VERTEX_POS_INDX, 3, GLES31.GL_FLOAT,
                false, 0, vertices)
        GLES31.glEnableVertexAttribArray(VERTEX_POS_INDX)

        //Now we are ready to draw the cube finally.
        var startPos = 0
        val verticesPerface = 6

        //draw front face
        GLES31.glUniform4fv(colorHandle, 1, GL_BLUE, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        startPos += verticesPerface

        //draw back face
        GLES31.glUniform4fv(colorHandle, 1, GL_CYAN, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        startPos += verticesPerface

        //draw left face
        GLES31.glUniform4fv(colorHandle, 1, GL_RED, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        startPos += verticesPerface

        //draw right face
        GLES31.glUniform4fv(colorHandle, 1, GL_GRAY, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        startPos += verticesPerface

        //draw top face
        GLES31.glUniform4fv(colorHandle, 1, GL_GREEN, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        startPos += verticesPerface

        //draw bottom face
        GLES31.glUniform4fv(colorHandle, 1, GL_YELLOW, 0)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, startPos, verticesPerface)
        //last face, so no need to increment.
    }

}