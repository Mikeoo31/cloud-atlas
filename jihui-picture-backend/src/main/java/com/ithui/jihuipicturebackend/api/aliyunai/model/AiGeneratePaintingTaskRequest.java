package com.ithui.jihuipicturebackend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

@Data
public class AiGeneratePaintingTaskRequest implements Serializable {

    /**
     * 模型，例如 "image-out-painting"
     */
    private String model = "wanx2.1-t2i-plus";

    /**
     * 输入图像信息
     */
    private AiGeneratePaintingTaskRequest.Input input;

    /**
     * 图像处理参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，图像 URL
         */
        @Alias("prompt")
        private String prompt;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，图片大小，取值范围 [1, 10000]，单位：px，默认值：1024
         */
        @Alias("size")
        private String size = "1024";

        /**
         * n integer （可选） 生成图片的数量。取值范围为1~4张，默认为4。修改为1
         */
        @Alias("n")
        private Integer n = 1;
    }
}
