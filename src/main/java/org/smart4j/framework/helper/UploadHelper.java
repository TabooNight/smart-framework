package org.smart4j.framework.helper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.bean.FileParam;
import org.smart4j.framework.bean.FormParam;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.FileUtil;
import org.smart4j.framework.util.StreamUtil;
import org.smart4j.framework.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件上传助手类
 */
public final class UploadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHelper.class);
    // Apache Commons FileUpload提供的Servlet文件上传对象
    private static ServletFileUpload servletFileUpload;

    /**
     * 初始化
     *
     * @param servletContext
     *                          ServletContext对象
     */
    public static void init(ServletContext servletContext) {

        // 获取应用服务器的临时目录作为上传文件的临时目录
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        servletFileUpload = new ServletFileUpload(new DiskFileItemFactory(
                DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository));
        int uploadLimit = ConfigHelper.getAppUploadLimit();
        if (uploadLimit != 0) {
            servletFileUpload.setFileSizeMax(uploadLimit * 1024 * 1024);// 设置文件上传大小限制
        }

    }

    /**
     * 判断请求是否为multipart理性
     *
     * @param request
     *                  HttpServletRequest
     * @return
     */
    public static boolean isMultipart(HttpServletRequest request) {

        return ServletFileUpload.isMultipartContent(request);

    }

    /**
     * 创建请求对象
     *
     * @param request
     *                  HttpServletRequest
     * @return
     *
     * @throws IOException
     */
    public static Param createParam(HttpServletRequest request) throws IOException {

        List<FormParam> formParamList = new ArrayList<FormParam>();
        List<FileParam> fileParamList = new ArrayList<FileParam>();

        // 获取所有参数集合
        try {
            Map<String, List<FileItem>> fileItemListMap = servletFileUpload.parseParameterMap(request);
            if (CollectionUtil.isNotEmpty(fileItemListMap)) {
                for (Map.Entry<String, List<FileItem>> fileItemListEntity : fileItemListMap.entrySet()) {
                    String fieldName = fileItemListEntity.getKey();// 获取参数名
                    List<FileItem> fileItemList = fileItemListEntity.getValue();// 获取参数值
                    if (CollectionUtil.isNotEmpty(fileItemList)) {
                        for (FileItem fileItem : fileItemList) {
                            if (fileItem.isFormField()) {// 普通表单数据
                                String fieldValue = fileItem.getString("UTF-8");
                                formParamList.add(new FormParam(fieldName, fieldValue));
                            } else {// 上传文件
                                String fileName = FileUtil.getRealFileName(new String(fileItem.getName()
                                        .getBytes(), "UTF-8"));
                                if (StringUtil.isNotEmpty(fileName)) {
                                    long fileSize = fileItem.getSize();
                                    String contentType = fileItem.getContentType();
                                    InputStream inputStream = fileItem.getInputStream();
                                    fileParamList.add(new FileParam(fieldName, fileName, fileSize,
                                            contentType, inputStream));
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileUploadException e) {
            LOGGER.error("create param failure", e);
            throw new RuntimeException(e);
        }
        return new Param(formParamList, fileParamList);

    }

    /**
     * 上传文件
     *
     * @param basePath
     *                  上传路径
     * @param fileParam
     *                  文件对象信息
     */
    public static void uploadFile(String basePath, FileParam fileParam) {

        try {
            if (fileParam != null) {
                String filePath = basePath + fileParam.getFileName();
                FileUtil.createFile(filePath);
                InputStream inputStream = new BufferedInputStream(fileParam.getInputStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                StreamUtil.copyStream(inputStream, outputStream);
            }
        } catch (Exception e) {
            LOGGER.error("upload file failure", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 批量上传文件
     *
     * @param basePath
     *                      上传路径
     * @param fileParamList
     *                      文件信息列表
     */
    public static void uploadFile(String basePath, List<FileParam> fileParamList) {

        try {
            if (CollectionUtil.isNotEmpty(fileParamList)) {
                for (FileParam fileParam: fileParamList) {
                    uploadFile(basePath, fileParam);
                }
            }
        } catch (Exception e) {
            LOGGER.error("upload file failure", e);
            throw new RuntimeException(e);
        }

    }

}
