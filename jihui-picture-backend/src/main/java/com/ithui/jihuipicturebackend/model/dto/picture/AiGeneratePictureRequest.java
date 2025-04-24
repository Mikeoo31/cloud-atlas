package com.ithui.jihuipicturebackend.model.dto.picture;

import com.ithui.jihuipicturebackend.api.aliyunai.model.AiGeneratePaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 接受前端传过来的数据
 */
@Data
public class AiGeneratePictureRequest implements Serializable {

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 图片参数
     */
    private AiGeneratePaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;

}
