/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

/**
 * The type S 3 utils.
 */
@Service
@RequiredArgsConstructor
public class S3Utils {
    private final AmazonS3 s3Client;
    private final AWSSettings awsSettings;

    /**
     * Upload file.
     *
     * @param objectName the object name
     * @param file       the file
     */
    public void uploadFile(String objectName, File file) {
        this.s3Client.putObject(this.awsSettings.bucket(), objectName, file);
    }
    public String getUploadUrl(String objectName) {
        return s3Client.getUrl(this.awsSettings.bucket(), objectName).toString();
    }
    /**
     * Gets pre signed url.
     *
     * @param objectName the object name
     * @return the pre signed url
     */
    public String getPreSignedUrl(String objectName) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 10000; // 10 seconds
        expiration.setTime(expTimeMillis);

        return this.s3Client.generatePresignedUrl(this.awsSettings.bucket(), objectName, expiration).toString();
    }

    /**
     * Gets object.
     *
     * @param key      the key
     * @param fileName the file name
     * @return the object
     */
    public File getObject(String key, String fileName) {
        File localFile = new File("/tmp/" + fileName);
        CommonUtils.deleteFile(localFile);
        this.s3Client.getObject(new GetObjectRequest(this.awsSettings.bucket(), key), localFile);
        return localFile;
    }
}
