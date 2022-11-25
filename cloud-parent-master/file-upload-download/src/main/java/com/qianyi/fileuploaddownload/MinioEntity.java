package com.qianyi.fileuploaddownload;

import io.minio.messages.Bucket;
import lombok.Data;

@Data
public class MinioEntity {
    private String endpoint;
    private Integer port=9000;
    private String accessKey="jsadmin";
    private String secretKey="jsadmin1";
    private String bucket;

    public MinioEntity(String endpoint, String bucket) {
        this.endpoint=endpoint;
        this.bucket=bucket;
    }

    public MinioEntity(String endpoint) {
        this(endpoint,"qianyi");
    }
}
