package com.st.sensor_fusion.utility

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.st.sensor_fusion.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.roundToInt

class  GLCubeRender : GLSurfaceView.Renderer {
    private val TAG = GLCubeRender::class.java.canonicalName

    // X, Y, Z
    private val CUBE_VERTEX_POSITION =
        floatArrayOf( // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,  // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,  // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,  // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,  // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,  // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f
        )

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    private val CUBE_TEXTURE_COORDINATE = floatArrayOf( // Front face
        0.0f, 0.0f,
        0.0f, 0.5f,
        1.0f / 3.0f, 0.0f,
        0.0f, 0.5f,
        1.0f / 3.0f, 0.5f,
        1.0f / 3.0f, 0.0f,  // Right face
        2.0f / 3.0f, 0.0f,
        2.0f / 3.0f, 0.5f,
        1.0f, 0.0f,
        2.0f / 3.0f, 0.5f,
        1.0f, 0.5f,
        1.0f, 0.0f,  // Back face
        2.0f / 3.0f, 0.5f,
        2.0f / 3.0f, 1.0f,
        1.0f, 0.5f,
        2.0f / 3.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.5f,  // Left face
        0.0f, 0.5f,
        0.0f, 1.0f,
        1.0f / 3.0f, 0.5f,
        0.0f, 1.0f,
        1.0f / 3.0f, 1.0f,
        1.0f / 3.0f, 0.5f,  // Top face
        1.0f / 3.0f, 0.0f,
        1.0f / 3.0f, 0.5f,
        2.0f / 3.0f, 0.0f,
        1.0f / 3.0f, 0.5f,
        2.0f / 3.0f, 0.5f,
        2.0f / 3.0f, 0.0f,  // Bottom face
        1.0f / 3.0f, 0.5f,
        1.0f / 3.0f, 1.0f,
        2.0f / 3.0f, 0.5f,
        1.0f / 3.0f, 1.0f,
        2.0f / 3.0f, 1.0f,
        2.0f / 3.0f, 0.5f
    )

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val mModelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val mViewMatrix = FloatArray(16)

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private val mProjectionMatrix = FloatArray(16)

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private val mMVPMatrix = FloatArray(16)

    /**
     * Store our model data in a float buffer.
     */
    private var mCubePositions: FloatBuffer

    /**
     * store our texture coordiante in a float buffer
     */
    private var  mCubeTextureCoordinates: FloatBuffer

    /**
     * This will be used to pass in the transformation matrix.
     */
    private var mMVPMatrixHandle = 0

    /**
     * This will be used to pass in model position information.
     */
    private var mPositionHandle = 0

    /**
     * This will be used to pass in model texture coordinate information.
     */
    private var mTextureCoordinateHandle = 0

    /**
     * This is a handle to our cube shading program.
     */
    private var mProgramHandle = 0

    /**
     * This is a handle to our texture data.
     */
    private var mTextureDataHandle = 0

    /**
     * context used for load the resources
     */
    private var mContext: Context? = null

    /**
     * background color
     */
    private var mBgColor = 0

    private val SCALE_CUBE_MAX = 0.9f
    private val SCALE_CUBE_MIN = 0.3f
    private val SCALE_CUBE_FACTOR = SCALE_CUBE_MAX - SCALE_CUBE_MIN
    private var scale_cube = SCALE_CUBE_MAX - SCALE_CUBE_MIN

    /**
     * matrix used as temporaney for compute things like a=a*b
     */
    private val mTempMultMatrix = FloatArray(16)

    /**
     * rotation to apply at the cube
     */
    private val mRotationMatrix = FloatArray(16)

    /**
     * rotation of the base postion
     */
    private val mInverseRotationMatrix = FloatArray(16)

    // variable used for compute the frame rate,
    private val MAX_HW_RENDERING_RATE_HZ = 60
    private var rendering_start_time: Long = 0
    private val RENDERING_RATE_WINDOW_SIZE = 16
    private val rendering_rate = IntArray(RENDERING_RATE_WINDOW_SIZE)
    private var rendering_rate_index = 0
    private var rendering_rate_avg = 0f

    constructor(c: Context?, color: Int) {
        mBgColor = color
        mContext = c

        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(CUBE_VERTEX_POSITION.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubePositions.put(CUBE_VERTEX_POSITION).position(0)
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(CUBE_TEXTURE_COORDINATE.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeTextureCoordinates.put(CUBE_TEXTURE_COORDINATE).position(0)
        Matrix.setIdentityM(mRotationMatrix, 0)
        Matrix.setIdentityM(mInverseRotationMatrix, 0)

        // Position the eye in front of the origin.
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 0.3f

        // We are looking toward the distance
        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
    }

    /**
     * load a texture into the opengl context
     *
     * @param context    application context used for load the resource
     * @param resourceId drawable id that represent the texture
     * @return opengl texture handle
     */
    fun loadTexture(context: Context?, @DrawableRes resourceId: Int): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false // No pre-scaling

            // Read in the resource
            val bitmap = BitmapFactory.decodeResource(
                context!!.resources,
                resourceId, options
            )

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            // Set filtering
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        return textureHandle[0]
    }

    /**
     * load a text file into a string
     *
     * @param context    application context
     * @param resourceId raw resource id, the resource will be read as text file
     * @return file content
     */
    private fun readTextFileFromRawResource(
        context: Context?,
        @RawRes resourceId: Int
    ): String? {
        val inputStream = context!!.resources.openRawResource(
            resourceId
        )
        val inputStreamReader = InputStreamReader(
            inputStream
        )
        val bufferedReader = BufferedReader(
            inputStreamReader
        )
        var nextLine: String?
        val body = StringBuilder()
        try {
            while (bufferedReader.readLine().also { nextLine = it } != null) {
                body.append(nextLine)
                body.append('\n')
            } //while
        } catch (e: IOException) {
            return null
        } //try-catch
        return body.toString()
    }

    /**
     * Helper function to compile a shader.
     *
     * @param shaderType   The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    private fun compileShader(shaderType: Int, shaderSource: String?): Int {
        var shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource)

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }
        if (shaderHandle == 0) {
            throw RuntimeException("Error creating shader.")
        }
        return shaderHandle
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private fun createAndLinkProgram(
        vertexShaderHandle: Int,
        fragmentShaderHandle: Int,
        attributes: Array<String>?
    ): Int {
        var programHandle = GLES20.glCreateProgram()
        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            // Bind attributes
            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle)

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle))
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }
        if (programHandle == 0) {
            throw RuntimeException("Error creating program.")
        }
        return programHandle
    }


    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        // Set the background clear color to black.
        GLES20.glClearColor(
            Color.red(mBgColor) / 255.0f, Color.green(mBgColor) / 255.0f,
            Color.blue(mBgColor) / 255.0f, Color.alpha(mBgColor) / 255.0f
        )

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        val vertexShaderHandle = compileShader(
            GLES20.GL_VERTEX_SHADER,
            readTextFileFromRawResource(mContext, R.raw.per_pixel_vertex_shader)
        )
        val fragmentShaderHandle = compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            readTextFileFromRawResource(mContext, R.raw.per_pixel_fragment_shader)
        )
        mProgramHandle = createAndLinkProgram(
            vertexShaderHandle,
            fragmentShaderHandle,
            arrayOf("a_Position", "a_TexCoordinate")
        )
        mTextureDataHandle = loadTexture(mContext, R.drawable.texture_cube_new)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height)

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f

        //Matrix.perspectiveM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.frustumM(mProjectionMatrix, 0, left, ratio, bottom, top, near, far)
    }

    private fun updateRenderingRate() {
        val rendering_end_time = System.currentTimeMillis()
        val rendering_period = rendering_end_time - rendering_start_time
        val rendering_rate_current =
            if (rendering_period == 0L) MAX_HW_RENDERING_RATE_HZ else (1000 / rendering_period).toFloat()
                .toInt()
        rendering_start_time = System.currentTimeMillis()
        rendering_rate_avg += (rendering_rate_current - rendering_rate[rendering_rate_index]).toFloat()
        rendering_rate[rendering_rate_index] = rendering_rate_current
        rendering_rate_index = (rendering_rate_index + 1) % RENDERING_RATE_WINDOW_SIZE
    }

    override fun onDrawFrame(glUnused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle)

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix")
        val textureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate")

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        //set clamp to edge for be secure that it works also with texture that are not a power of 2
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0)

        // Draw some cubes.
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.0f)
        Matrix.scaleM(mModelMatrix, 0, scale_cube, scale_cube, scale_cube)
        synchronized(this) {
            Matrix.multiplyMM(
                mTempMultMatrix,
                0,
                mModelMatrix,
                0,
                mInverseRotationMatrix,
                0
            )
            Matrix.multiplyMM(mModelMatrix, 0, mTempMultMatrix, 0, mRotationMatrix, 0)
        }
        drawCube()
        updateRenderingRate()
    }

    /**
     * Draws a cube.
     */
    private fun drawCube() {
        // Pass in the position information
        mCubePositions.position(0)
        GLES20.glVertexAttribPointer(
            mPositionHandle, 3, GLES20.GL_FLOAT, false,
            0, mCubePositions
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0)
        GLES20.glVertexAttribPointer(
            mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,
            0, mCubeTextureCoordinates
        )
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mTempMultMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mTempMultMatrix, 0)

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }


    /**
     * change the cube rotation
     * @param qi quaternion x component
     * @param qj quaternion y component
     * @param qk quaternion z component
     * @param qs quaternion scalar component
     */
    fun setRotation(qi: Float, qj: Float, qk: Float, qs: Float) {
        synchronized(this) {
            SensorManager.getRotationMatrixFromVector(
                mRotationMatrix,
                floatArrayOf(-qi, -qj, -qk, qs)
            )
        }
    }

    /**
     * get the surface fps, it is a moving average with a windows of  16 samples
     * @return number of onDrawFrame call per seconds
     */
    fun getRenderingRate(): Int {
        val rendering_rate = (rendering_rate_avg / RENDERING_RATE_WINDOW_SIZE).roundToInt()
        return Math.min(rendering_rate, MAX_HW_RENDERING_RATE_HZ)
    }

    /**
     * change the cube zoom factor
     * @param scale scale factor between 0 and 1
     */
    fun setScaleCube(scale: Float) {
        scale_cube = SCALE_CUBE_MIN + SCALE_CUBE_FACTOR * scale
    }

    /**
     * set the current rotation as the base rotation
     */
    fun resetCube() {
        synchronized(this) {
            mInverseRotationMatrix[0] = mRotationMatrix[0]
            mInverseRotationMatrix[1] = mRotationMatrix[4]
            mInverseRotationMatrix[2] = mRotationMatrix[8]
            mInverseRotationMatrix[3] = mRotationMatrix[3]
            mInverseRotationMatrix[4] = mRotationMatrix[1]
            mInverseRotationMatrix[5] = mRotationMatrix[5]
            mInverseRotationMatrix[6] = mRotationMatrix[9]
            mInverseRotationMatrix[7] = mRotationMatrix[7]
            mInverseRotationMatrix[8] = mRotationMatrix[2]
            mInverseRotationMatrix[9] = mRotationMatrix[6]
            mInverseRotationMatrix[10] = mRotationMatrix[10]
            mInverseRotationMatrix[11] = mRotationMatrix[11]
            mInverseRotationMatrix[12] = mRotationMatrix[12]
            mInverseRotationMatrix[13] = mRotationMatrix[13]
            mInverseRotationMatrix[14] = mRotationMatrix[14]
            mInverseRotationMatrix[15] = mRotationMatrix[15]
        }
    }
}