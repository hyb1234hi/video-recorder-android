package com.kalu.recorder.GlRender;

import android.content.Context;
import android.opengl.GLES30;

import com.kalu.recorder.Utils.StringManagerUtil;
import com.kalu.recorder.GlRenderBase.GlRenderNormalFBO;
import com.kalu.recorder.R;

/**
 * Created by Lzc on 2018/3/19 0019.
 */

public class GlRenderBrightness extends GlRenderNormalFBO {
    private int mBrightnessLoc;

    public GlRenderBrightness(Context context) {
        super(context);
        mBrightnessLoc = GLES30.glGetUniformLocation(mProgram, "brightness");
        setFloat(mBrightnessLoc, 0.2f);
    }

    @Override
    public String getFragmentShaderCode() {
        return StringManagerUtil.getStringFromRaw(context, R.raw.brigthness_fragment);
    }

    @Override
    public String getVertexShaderCode() {
        return StringManagerUtil.getStringFromRaw(context, R.raw.camera_vertex_shader);
    }


    @Override
    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }
}
