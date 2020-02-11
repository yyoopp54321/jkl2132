package com.example.jkl.service.impl;

import com.example.jkl.service.FileService;
import com.example.jkl.service.FtpService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service
public class FileServiceImpl implements FileService {
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FtpService ftpService;

    public String upload(MultipartFile file, String uploadPath) {
        String originalFilename = file.getOriginalFilename();
        String extendName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String fileName = UUID.randomUUID().toString() + "." + extendName;
        logger.info("开始上传文件，上传文件名：{}，上传后文件名：{}，文件路径：{}",originalFilename, fileName, extendName);
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.setWritable(true);
            uploadDir.mkdirs();
        }
        File uploadFile = new File(uploadDir,fileName);
        try {
            // 上传到tomcat
            file.transferTo(uploadFile);

            //上传文件到FTP服务器
            ftpService.upload(Lists.newArrayList(uploadFile));

            // 删除上传到tomcat的文件
            uploadFile.delete();
        } catch (IOException e) {
            logger.error("上传文件失败", e);
        }
        return uploadFile.getName();
    }
}
