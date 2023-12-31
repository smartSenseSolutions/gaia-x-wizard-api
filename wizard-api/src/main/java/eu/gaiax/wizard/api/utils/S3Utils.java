/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

import static eu.gaiax.wizard.api.utils.StringPool.TEMP_FOLDER;

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
        this.s3Client.listBuckets();
        this.s3Client.putObject(this.awsSettings.bucket(), objectName, file);
    }

    public void deleteFile(String objectName) {
        this.s3Client.deleteObject(this.awsSettings.bucket(), objectName);
    }

    public void uploadFileWithPublicAcl(String objectName, File file) {
        PutObjectRequest request = new PutObjectRequest(this.awsSettings.bucket(), objectName, file);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        this.s3Client.putObject(request);
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
        expTimeMillis += 20_000; // 20 seconds
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
        File localFile = new File(TEMP_FOLDER + fileName);
        CommonUtils.deleteFile(localFile);
        this.s3Client.getObject(new GetObjectRequest(this.awsSettings.bucket(), key), localFile);
        return localFile;
    }

    public String getObject(String fileName) {
        try {
            return this.s3Client.getUrl(this.awsSettings.bucket(), fileName).toString();
        } catch (Exception e) {
            throw new BadDataException("not.able.to.get.file");
        }
    }

}
