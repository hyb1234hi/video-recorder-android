package com.kalu.recorder.GlRender;

import android.content.Context;
import android.opengl.GLES30;

import com.kalu.recorder.GlRenderBase.GlRenderNormalFBO;
import com.kalu.recorder.Utils.StringManagerUtil;
import com.kalu.recorder.R;

import java.nio.FloatBuffer;

/**
 * Created by Lzc on 2018/3/12 0012.
 */

public class GlRenderFBODefault extends GlRenderNormalFBO {

    public GlRenderFBODefault(Context context) {
        super(context);
    }

    @Override
    public String getFragmentShaderCode() {

        return StringManagerUtil.getStringFromRaw(context,
                R.raw.normal_fragment_shader);
    }


    @Override
    public String getVertexShaderCode() {
        return StringManagerUtil.getStringFromRaw(context, R.raw.camera_vertex_shader);
    }

    @Override
    public int drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {

        return super.drawFrame(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public void onDrawArraysBegin() {

    }

    @Override
    public void onDrawArraysAfter() {

    }


    @Override
    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

}
