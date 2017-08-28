package org.smart4j.framework.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 文件操作工具类
 */
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 获取真实文件名(自动去掉文件路径)
     *
     * @param fileName
     *                  文件名
     * @return
     */
    public static String getRealFileName(String fileName) {

        return FilenameUtils.getName(fileName);

    }

    public static File createFile(String filePath) {

        File file;
        try {
            file = new File(filePath);// 创建文件
            File parentDir = file.getParentFile();// 获取所在文件夹
            if (!parentDir.exists()) {// 不存在则创建
                FileUtils.forceMkdir(parentDir);
            }
        } catch (Exception e) {
            LOGGER.error("create file failure", e);
            throw new RuntimeException(e);
        }
        return file;

    }

}
