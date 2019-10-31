package shamu.company.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.exception.AwsException;
import shamu.company.common.exception.response.ErrorType;

@Component
@Slf4j
public class AwsUtil {

  private static final long expires = 1000 * 60 * 60 * 6;

  @Value("${aws.bucketName}")
  private String bucketName;

  @Value("${aws.folder}")
  private String folder;

  private AmazonS3 getClient() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  private File generateFile(final String originalFilename) throws IOException {
    final String projectDir = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX).getPath();
    final String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

    final String fileName = String.format("%suploads/%s%s", projectDir,
        System.currentTimeMillis(),
        UUID.randomUUID().toString());
    return File.createTempFile(fileName, suffix);
  }

  public String uploadFile(final MultipartFile multipartFile) throws IOException {
    return this.uploadFile(multipartFile, Type.DEFAULT);
  }

  public String uploadFile(@NotNull final MultipartFile multipartFile, final Type type)
      throws IOException {
    final File file = this.generateFile(multipartFile.getOriginalFilename());
    if (multipartFile.isEmpty()) {
      throw new AwsException("File is empty.");
    }
    try {
      multipartFile.transferTo(file);
    } catch (final IOException e) {
      log.error(String.format("Caught an exception while uploading file: %s", e.getMessage()));
      throw new AwsException("Failed to upload file!");
    }

    final String path = this.uploadFile(file.getPath(), type);
    return path;
  }

  public String uploadFile(final String filePath, final Type type) {
    final AmazonS3 s3Client = this.getClient();
    final File file = new File(filePath);
    final String fileName = file.getName();
    final String path = getSavePathInAws(fileName, type);
    if (!file.exists() || !file.isFile()) {
      throw new AwsException("The file: " + filePath + " doesn't exist.");
    }

    try {
      log.info("=====Uploading an object to S3 from a file start=====");
      s3Client.putObject(new PutObjectRequest(bucketName, path, file));
      log.info("=====Uploading an object to S3 from a file end  =====");

    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3,"
              + " but was rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      log.error(String.format("HTTP Status Code: %s", ase.getStatusCode()));
      log.error(String.format("AWS Error Code:   %s", ase.getErrorCode()));
      log.error(String.format("Error Type:       %s", ase.getErrorType()));
      log.error(String.format("Request ID:       %s", ase.getRequestId()));
      throw new AwsException("Failed to upload file!");
    } catch (final AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error"
              + " while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsException("Failed to upload file!");
    }

    this.deleteFile(file);
    return path;
  }

  private String getSavePathInAws(final String fileName, final Type type) {
    final Calendar now = Calendar.getInstance();
    final String year = String.valueOf(now.get(Calendar.YEAR));
    final String month = String.valueOf(now.get(Calendar.MONTH) + 1);
    final String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

    return String.format("%s/%s/%s/%s/%s/%s", folder, type.folder, year, month, day, fileName);
  }

  public String moveFileFromTemp(final String path) {
    final AmazonS3 amazonS3 = this.getClient();
    try {
      final String key = path.replace("/temp", "/uploads");
      log.info("move file from /temp");
      amazonS3.copyObject(new CopyObjectRequest(bucketName, path, bucketName, key));
      amazonS3.deleteObject(bucketName, path);
      return key;
    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but was"
              + " rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      throw new AwsException(ase.getMessage());
    } catch (final AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error "
              + "while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsException(ace.getMessage());
    }
  }

  public void deleteFile(final File... files) {
    for (final File file : files) {
      if (file.exists()) {
        file.delete();
      }
    }
  }

  public void deleteFile(final String path) {
    final AmazonS3 amazonS3 = this.getClient();
    try {
      log.info("delete file from Amazon S3");
      amazonS3.deleteObject(bucketName, path);
    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but "
              + "was rejected with an error response for some reason.");
      log.error(String.format("Error Message:    %s", ase.getMessage()));
      throw new AwsException(ase.getMessage());
    } catch (final AmazonClientException ace) {
      log.error(
          "Caught an AmazonClientException, which means the client encountered an internal error "
              + "while trying to communicate with S3, "
              + "such as not being able to access the network.");
      log.error(String.format("Error Message: %s", ace.getMessage()));
      throw new AwsException(ace.getMessage());
    }
  }

  public String getPreSignedUrl(final String key) {
    try {
      final AmazonS3 s3Client = getClient();

      // Set the pre-signed URL to expire after one hour.
      final java.util.Date expiration = new java.util.Date();
      final long expTimeMillis = expiration.getTime() + expires;
      expiration.setTime(expTimeMillis);

      // Generate the pre-signed URL.
      final GeneratePresignedUrlRequest generatePresignedUrlRequest =
          new GeneratePresignedUrlRequest(bucketName, key)
              .withMethod(HttpMethod.GET)
              .withExpiration(expiration);
      final URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

      return url.toString();
    } catch (final AmazonServiceException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      throw new AwsException("Failed to get url of file!", ErrorType.AWS_GET_URL_EXCEPTION, e);
    } catch (final SdkClientException e) {
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      throw new AwsException("Failed to get url of file!", ErrorType.AWS_GET_URL_EXCEPTION, e);
    }
  }

  public String getAwsPath() {
    return "https://" + this.bucketName + ".s3.amazonaws.com/";
  }

  public String getFullFileUrl(final String path) {
    return this.getAwsPath() + path;
  }

  public enum Type {
    TEMP("temp"), IMAGE("image"), DEFAULT("uploads");

    String folder;

    Type(final String folder) {
      this.folder = folder;
    }
  }
}
