package com.CU.wenxing.LJClusterOpt.display.util;

import android.opengl.GLES20;
import android.util.Log;

import static com.CU.wenxing.LJClusterOpt.Parameters.LoggerConfigOn;

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    private static int compileShader(int type, String shaderCode){
        final int shader = GLES20.glCreateShader(type);

        if(shader == 0){
            if(LoggerConfigOn){
                Log.w(TAG, "Could not create new shader");
            }
            return 0;
        }

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(LoggerConfigOn){
            Log.v(TAG, "Results of compiling source:\n" + shaderCode + "\n" + GLES20.glGetShaderInfoLog(shader));
        }
        if(compileStatus[0] == 0){
            GLES20.glDeleteShader(shader);
            if(LoggerConfigOn){
                Log.w(TAG, "Compilation of shader failed.");
            }
            return 0;
        }

        return shader;
    }

    public static int compileVertexShader(String shaderCode){
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode){
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int linkProgram(int vertexShader, int fragmentShader){
        final int program = GLES20.glCreateProgram();

        if(program == 0){
            if(LoggerConfigOn){
                Log.w(TAG, "Could not create new program");
            }
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if(LoggerConfigOn){
            Log.v(TAG, "Results of linking program:\n" + GLES20.glGetProgramInfoLog(program));
        }
        if(linkStatus[0] == 0){
            GLES20.glDeleteProgram(program);
            if(LoggerConfigOn){
                Log.w(TAG, "Linking of program failed.");
            }
            return 0;
        }

        return program;
    }

    /**
     * validate an OpenGL program. Should only be called when developing the application
     */
    public static boolean validateProgram(int program){
        GLES20.glValidateProgram(program);

        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0] + "\nLog: " + GLES20.glGetProgramInfoLog(program));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vetexShaderSource, String fragmentShaderSource){
        int vertexShader = compileVertexShader(vetexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        int program = linkProgram(vertexShader, fragmentShader);

        if(LoggerConfigOn){
            validateProgram(program);
        }
        return program;
    }

}
