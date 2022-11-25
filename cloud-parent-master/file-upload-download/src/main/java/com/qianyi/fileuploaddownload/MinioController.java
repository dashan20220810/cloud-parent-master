package com.qianyi.fileuploaddownload;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.minio.*;
import io.minio.http.Method;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@RestController
@RequestMapping("minio")
@Api(tags = "文件(图片)上传与下载")
@Slf4j
public class MinioController {

    private static MinioClient client;

    @Value("${minio.endpoint:localhost}")
    private String endpoint;
    @Value("${minio.secretKey}")
    private String configSecretKey;

    //endpoint: 域名或IP  ，bucket: 不同项目不同名称
    private MinioClient getInstance() {
        if (client == null) {
            MinioEntity entity = new MinioEntity(endpoint);

            client = MinioClient.builder().endpoint(entity.getEndpoint(), entity.getPort(), false).credentials(entity.getAccessKey(), entity.getSecretKey()).build();
        }
        return client;
    }

    @ApiOperation("文件（图片）上传,成功返回文件地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bigFileSecret", type = "String", value = "大文件(2M以上)需要秘钥。找管理员获取", required = false),
            @ApiImplicitParam(name = "bucket", type = "String", value = "bucket名称,提前联系管理员获取", required = true),
    })
    @PostMapping("upload/{bucket}")
    public ResponseEntity<String> upload(@PathVariable("bucket") String bucket, @RequestPart("file") MultipartFile file, String bigFileSecret) {
        if (file == null) {
            return ResponseUtil.custom("文件不能为null");
        }
        try {
            if (!checkBucketExists(bucket)) {
                return ResponseUtil.custom("不支持，请联系技术");
            }
            InputStream inputStream = file.getInputStream();
            long size = inputStream.available();
            System.out.println("文件大小：" + size + " Byte");

            if (CommonUtil.checkNull(bigFileSecret) || !checkBigFileSecret(bigFileSecret)) {
                if (size == 0 || size > 2 * 1024 * 1024) {
                    return ResponseUtil.custom("文件不能大于2M");
                }
            }

            String oriFilename = file.getOriginalFilename();
            String[] split = oriFilename.split("\\.");
            String suffix = split[split.length - 1];
            String filename = UUID.randomUUID().toString() + "." + suffix;
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucket).object(filename).stream(inputStream, size, -1).build();
            //上传到MINIO
            getInstance().putObject(args);

            //http://10.0.2.15:9000/qianyi/16cc42a1-84ba-48b4-9cc8-9cfa56406f15.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=A762M2VP3HO3IC9FALXZ%2F20211110%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20211110T051859Z&X-Amz-Expires=604799&X-Amz-Security-Token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NLZXkiOiJBNzYyTTJWUDNITzNJQzlGQUxYWiIsImV4cCI6MTYzNjUyMjgyMCwicG9saWN5IjoiY29uc29sZUFkbWluIn0.wt__9QLh-fq8YRWQwvcj8nro9x8CrF8sOSqpNPXJKfh7TPaZU7F7lcm_FkwMejCRcQKXkjhuGiUphYVrmGh0LQ&X-Amz-SignedHeaders=host&versionId=null&X-Amz-Signature=92b6592256640439d6c3a50d244951501ee2438f37d5e439f539cfd0152da8a7
            String url = getInstance().getPresignedObjectUrl(new GetPresignedObjectUrlArgs().builder()
                    .bucket(bucket).object(filename).method(Method.GET).build());
            String[] urlStr = url.split("\\?");

            inputStream.close();
            if (urlStr == null || url.length() == 0 || ObjectUtils.isEmpty(urlStr[0])) {
                log.error("urlStr为null");
                return ResponseUtil.success();
            }
            URL path = new URL(urlStr[0]);
            return ResponseUtil.success(path.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail();
        }

    }

    private boolean checkBigFileSecret(String bigFileSecret) {

        if (bigFileSecret.equals("jsjs")) {
            return true;
        }

        return false;
    }

    //创建bucket
    @PostMapping("createBucket")
    @ApiOperation("创建bucket")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", type = "String", value = "bucket名称", required = true),
            @ApiImplicitParam(name = "secretKey", type = "String", value = "秘钥", required = true),
    })
    public ResponseEntity createBucket(String name, String secretKey, HttpServletRequest request) {
        if (CommonUtil.checkNull(name) || name.length() < 3 || name.length() > 63) {
            return ResponseUtil.custom("名称长度3-63个字符");
        }
        if (!configSecretKey.equals(secretKey)) {
            return ResponseUtil.custom("秘钥无效");
        }
        try {

            if (checkBucketExists(name)) {
                return ResponseUtil.custom("bucket 已存在");
            }

            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(name).build();
            getInstance().makeBucket(makeBucketArgs);

            log.info("{},创建bucketName: {}", IpUtil.getIp(request), name);
            return ResponseUtil.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail();
        }
    }


    private boolean checkBucketExists(String bucket) throws Exception {
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucket).build();
        return getInstance().bucketExists(bucketExistsArgs);
    }
}
