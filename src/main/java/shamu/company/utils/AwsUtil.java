package shamu.company.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.exception.AwsUploadException;

@Component
@Slf4j
public class AwsUtil {

  @Value("${aws.awsAccessKeyId}")
  private String awsAccessKeyId;

  @Value("${aws.awsSecretAccessKey}")
  private String awsSecretAccessKey;

  @Value("${aws.bucketName}")
  private String bucketName;

  @Value("${aws.folder}")
  private String folder;

  @Value("${aws.region}")
  private String region;


  public enum Type {
    TEMP("temp"), IMAGE("image"), DEFAULT("uploads");

    String folder;

    Type(String folder) {
      this.folder = folder;
    }
  }

  private AmazonS3 getClient() {
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    return AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        .build();
  }

  private String generateFilePath(String originalFilename) throws FileNotFoundException {
    String projectDir = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX).getPath();
    String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

    return String.format("%suploads/%s%s%s", projectDir,
            System.currentTimeMillis(),
            UUID.randomUUID().toString(), suffix);
  }

  public String uploadFile(MultipartFile multipartFile) throws IOException {
    return this.uploadFile(multipartFile, Type.DEFAULT);
  }

  public String uploadFile(@NotNull MultipartFile multipartFile, Type type) throws IOException {
    String fileName = this.generateFilePath(multipartFile.getOriginalFilename());
    String prefix = fileName.substring(fileName.lastIndexOf("."));
    File file = File.createTempFile(UUID.randomUUID().toString(), prefix);
    if (multipartFile.isEmpty()) {
      throw new AwsUploadException("File is empty");
    }
    try {
      multipartFile.transferTo(file);
    } catch (IOException e) {
      log.error(String.format("Caught an exception while uploading file: %s", e.getMessage()));
      throw new AwsUploadException("Failed to upload file!");
    }

    String path = this.uploadFile(file.getPath(), type);
    return path;
  }

  public String uploadFile(String filePath, Type type) {
    AmazonS3 s3Client = this.getClient();
    File file = new File(filePath);
    String fileName = file.getName();
    String path = getSavePathInAws(fileName, type);
    if (!file.exists() || !file.isFile()) {
      throw new AwsUploadException("The file: " + filePath + " doesn't exist");
    }

    try {
      log.info("=====Uploading an object to S3 from a file start=====");
      s3Client.putObject(new PutObjectRequest(bucketName, path, file)
          .withCannedAcl(CannedAccessControlList.PublicRead));
      log.info("=====Uploading an object to S3 from a file end  =====");

    } catch (AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3,"
              + " but was rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      log.error(String.format("HTTP Status Code: %s", ase.getStatusCode()));
      log.error(String.format("AWS Error Code:   %s", ase.getErrorCode()));
      log.error(String.format("Error Type:       %s", ase.getErrorType()));
      log.error(String.format("Request ID:       %s", ase.getRequestId()));
      throw new AwsUploadException("Failed to upload file!");
    } catch (AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error"
              + " while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsUploadException("Failed to upload file!");
    }

    this.deleteFile(file);
    return path;
  }

  private String getSavePathInAws(String fileName, Type type) {
    Calendar now = Calendar.getInstance();
    String year = String.valueOf(now.get(Calendar.YEAR));
    String month = String.valueOf(now.get(Calendar.MONTH) + 1);
    String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

    return String.format("%s/%s/%s/%s/%s/%s", folder, type.folder, year, month, day, fileName);
  }

  public String moveFileFromTemp(String path) {
    AmazonS3 amazonS3 = this.getClient();
    try {
      String key = path.replace("/temp", "/uploads");
      log.info("move file from /temp");
      amazonS3.copyObject(new CopyObjectRequest(bucketName, path, bucketName, key)
          .withCannedAccessControlList(CannedAccessControlList.PublicRead));
      amazonS3.deleteObject(bucketName, path);
      return key;
    } catch (AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but was"
              + " rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      throw new AwsUploadException(ase.getMessage());
    } catch (AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error "
              + "while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsUploadException(ace.getMessage());
    }
  }

  public void deleteFile(File... files) {
    for (File file : files) {
      if (file.exists()) {
        file.delete();
      }
    }
  }

  public void deleteFile(String path) {
    AmazonS3 amazonS3 = this.getClient();
    try {
      log.info("delete file from Amazon S3");
      amazonS3.deleteObject(bucketName, path);
    } catch (AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but "
              + "was rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      throw new AwsUploadException(ase.getMessage());
    } catch (AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error "
              + "while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsUploadException(ace.getMessage());
    }
  }
}
