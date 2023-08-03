package eu.gaiax.wizard.api.utils;


import com.amazonaws.AmazonClientException;
        import com.amazonaws.auth.AWSStaticCredentialsProvider;
        import com.amazonaws.auth.BasicAWSCredentials;
        import com.amazonaws.regions.Regions;
        import com.amazonaws.services.s3.AmazonS3;
        import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
        import com.amazonaws.services.s3.model.PutObjectRequest;
        import java.io.File;
import java.util.List;

public class TestS3 {

    public static void main(String[] args) {
        String bucketName = "smartsense-gaiax-mvp"; // Replace with your S3 bucket name
        String key = "file.json"; // Replace with the desired key/file name in S3
        String filePath = "/home/mittal/service_request.json"; // Replace with the path to your JSON file

        // Set up AWS credentials
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIATDHX5KGO244UYGXC", "1Q30HrIhRH/e4SaVHWE8GGAqZxfsAekVw4yaFME5");

        // Create S3 client
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1) // Replace with your desired region
                .build();

        try {
            List<Bucket> buckets = s3client.listBuckets();
            for (Bucket bucket : buckets) {
                if(bucket.getName().equals(bucketName)){
                    System.out.println("Avl");
                }
            }

            // Create metadata for the object (content type, content length, etc.)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/json"); // Set the content type of JSON file

            // Upload the file to S3
            PutObjectRequest request = new PutObjectRequest(bucketName, key, new File(filePath));
            request.setMetadata(metadata);

            s3client.putObject(request);
            System.out.println("File uploaded successfully!");
        } catch (AmazonClientException e) {
            e.printStackTrace();
        }
    }
}
