package cn.co.willow.android.ultimate.gpuimage.core_render_filter.filter_3x3_sampling;

import android.opengl.GLES30;

/**
 * 拉普拉斯算子滤镜，噪音敏感
 * 通常的分割算法都是把Laplacian 算子和平滑算子结合起来生成一个新的模板
 */
public class GPUImageLaplacianFilter extends GPUImage3x3TextureSamplingFilter {
    public static final String LAPLACIAN_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "uniform mediump mat3 convolutionMatrix;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "mediump vec3 bottomColor = texture2D(inputImageTexture, bottomTextureCoordinate).rgb;\n" +
            "mediump vec3 bottomLeftColor = texture2D(inputImageTexture, bottomLeftTextureCoordinate).rgb;\n" +
            "mediump vec3 bottomRightColor = texture2D(inputImageTexture, bottomRightTextureCoordinate).rgb;\n" +
            "mediump vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "mediump vec3 leftColor = texture2D(inputImageTexture, leftTextureCoordinate).rgb;\n" +
            "mediump vec3 rightColor = texture2D(inputImageTexture, rightTextureCoordinate).rgb;\n" +
            "mediump vec3 topColor = texture2D(inputImageTexture, topTextureCoordinate).rgb;\n" +
            "mediump vec3 topRightColor = texture2D(inputImageTexture, topRightTextureCoordinate).rgb;\n" +
            "mediump vec3 topLeftColor = texture2D(inputImageTexture, topLeftTextureCoordinate).rgb;\n" +
            "\n" +
            "mediump vec3 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];\n" +
            "resultColor += leftColor * convolutionMatrix[1][0] + centerColor.rgb * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];\n" +
            "resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];\n" +
            "\n" +
            "// Normalize the results to allow for negative gradients in the 0.0-1.0 colorspace\n" +
            "resultColor = resultColor + 0.5;\n" +
            "\n" +
            "gl_FragColor = vec4(resultColor, centerColor.a);\n" +
            "}\n";

    private float[] mConvolutionKernel;
    private int mUniformConvolutionMatrix;

    public GPUImageLaplacianFilter() {
        this(new float[]{
                0.5f, 1.0f, 0.5f,
                1.0f, -6.0f, 1.0f,
                0.5f, 1.0f, 0.5f
        });
    }

    private GPUImageLaplacianFilter(final float[] convolutionKernel) {
        super(LAPLACIAN_FRAGMENT_SHADER);
        mConvolutionKernel = convolutionKernel;
    }

    @Override
    public void onInit() {
        super.onInit();
        mUniformConvolutionMatrix = GLES30.glGetUniformLocation(getProgram(), "convolutionMatrix");
        setConvolutionKernel(mConvolutionKernel);
    }

    private void setConvolutionKernel(final float[] convolutionKernel) {
        mConvolutionKernel = convolutionKernel;
        setUniformMatrix3f(mUniformConvolutionMatrix, mConvolutionKernel);
    }
}
