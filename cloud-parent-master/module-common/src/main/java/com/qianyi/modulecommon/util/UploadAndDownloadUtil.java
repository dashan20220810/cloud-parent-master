package com.qianyi.modulecommon.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * 文件工具类
 */
@Slf4j
public class UploadAndDownloadUtil {

    private static String urlUpload= "/minio/upload/casino-admin";
    /**
     * 图片上传 basePath  PreReadUploadConfig.getBasePath
     */
    public static  String  fileUpload(MultipartFile file,String uploadUrl){
        log.info("doPost图片上传请求路径{}",uploadUrl+urlUpload);
        log.info("doPost图片上传请求参数{}",file);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = "";
        try {
            String fileName = file.getOriginalFilename();
            HttpPost httpPost = new HttpPost(uploadUrl+urlUpload);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), ContentType.MULTIPART_FORM_DATA, fileName);// 文件流
            //参数设置编码utf-8，不然中文会乱码
            ContentType contentType = ContentType.create("text/plain", Charset.forName("UTF-8"));
            builder.addTextBody("filename", fileName,contentType);// 类似浏览器表单提交，对应input的name和value
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(4000).setConnectionRequestTimeout(1000).setSocketTimeout(3000)
                    .build();
            httpPost.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(httpPost);// 执行提交
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                // 将响应内容转换为字符串
                String  returnContent= EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
                log.info("doPost请求图片上传返回参数{}",returnContent);
                JSONObject parse = JSONObject.parseObject(returnContent);
                Object data = parse.get("data");
                result = (String) data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void uploadFiles(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }
    /**
     * 创建新的文件名
     */
    public static String renameToUUID(String fileName) {
        return UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}
