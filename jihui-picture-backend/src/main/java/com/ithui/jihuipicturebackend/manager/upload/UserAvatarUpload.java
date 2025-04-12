package com.ithui.jihuipicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.ithui.jihuipicturebackend.exception.BusinessException;
import com.ithui.jihuipicturebackend.exception.ErrorCode;
import com.ithui.jihuipicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class UserAvatarUpload extends PictureUploadTemplate{
    @Override
    public void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 校验文件是否为空
        ThrowUtils.throwIf(multipartFile == null,new BusinessException(ErrorCode.PARAMS_ERROR,"文件不能为空"));

        // 校验文件大小
        final long MAX_FILE_SIZE = 1024 * 1024 * 1; // 1M
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE,
                new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小不能超过1M"));
        // 校验上传路径前缀
//        ThrowUtils.throwIf(uploadPathPrefix == null || uploadPathPrefix.isEmpty(),
//                new BusinessException(ErrorCode.PARAMS_ERROR,"上传路径前缀不能为空"));
        // 校验文件类型
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        List<String> uploadPathPrefixes = Arrays.asList("jpg", "png", "jpeg", "gif");
        ThrowUtils.throwIf(!uploadPathPrefixes.contains(fileSuffix),
                new BusinessException(ErrorCode.PARAMS_ERROR,"文件类型不正确"));
    }

    @Override
    public String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    public void processFile(Object inputSource, File tempFile) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(tempFile);
    }
}