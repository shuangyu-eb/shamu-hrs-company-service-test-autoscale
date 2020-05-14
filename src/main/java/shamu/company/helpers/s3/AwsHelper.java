package shamu.company.helpers.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.exception.AwsException;
import shamu.company.common.exception.response.ErrorType;

@Component
@Slf4j
public class AwsHelper {

  private static final long EXPIRES = 1000 * 60 * 60 * 6L;

  private static final String TEMP_URL = "/temp";

  private static final String UPLOADS = "/uploads";

  private static final String FAILED_TO_UPLOAD_FILE = "Failed to upload file.";

  private static final String ERROR_MESSAGE_FORMAT = "Error Message:    %s";

  private final String bucketName;

  private final String folder;

  @Autowired
  public AwsHelper(
      @Value("${aws.bucketName}") final String bucketName,
      @Value("${aws.folder}") final String folder) {
    this.bucketName = bucketName;
    this.folder = folder;
  }

  private AmazonS3 findClient() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  private File generateFile(final String originalFilename) {
    try {
      final String projectDir = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX).getPath();
      final String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));

      final String fileName =
          String.format(
              "%suploads/%s%s",
              projectDir, System.currentTimeMillis(), UUID.randomUUID().toString());
      return File.createTempFile(fileName, suffix);
    } catch (final FileNotFoundException e) {
      throw new AwsException("The file: " + originalFilename + " doesn't exist.");
    } catch (final IOException e) {
      throw new AwsException("Create the temp file failed.");
    }
  }

  public void amazonS3DeleteObject(final List<String> keys) {
    if (keys.isEmpty()) {
      return;
    }
    final String[] documentIds = keys.toArray(new String[keys.size()]);
    final AmazonS3 s3Client = findClient();
    final DeleteObjectsRequest dor = new DeleteObjectsRequest(bucketName).withKeys(documentIds);
    s3Client.deleteObjects(dor);
  }

  public String uploadFile(final MultipartFile multipartFile) {
    return uploadFile(multipartFile, Type.DEFAULT);
  }

  public String uploadFile(final MultipartFile multipartFile, final AccessType accessType) {
    return uploadFile(multipartFile, Type.DEFAULT, accessType);
  }

  public String uploadFile(@NotNull final MultipartFile multipartFile, final Type type) {
    return uploadFile(multipartFile, type, AccessType.PUBLIC_READ);
  }

  public String uploadFile(
      @NotNull final MultipartFile multipartFile, final Type type, final AccessType accessType) {
    final File file = generateFile(multipartFile.getOriginalFilename());
    if (multipartFile.isEmpty()) {
      throw new AwsException("File is empty.");
    }
    try {
      multipartFile.transferTo(file);
    } catch (final IOException e) {
      log.error(String.format("Caught an exception while uploading file: %s", e.getMessage()));
      throw new AwsException(FAILED_TO_UPLOAD_FILE);
    }

    return uploadFile(file.getPath(), type, accessType);
  }

  public String uploadFile(final String filePath, final Type type) {
    return uploadFile(filePath, type, AccessType.PUBLIC_READ);
  }

  public String uploadFile(final String filePath, final Type type, final AccessType accessType) {
    final AmazonS3 s3Client = findClient();
    final File file = new File(filePath);
    final String fileName = file.getName();
    final String path = findSavePathInAws(fileName, type);
    if (!file.exists() || !file.isFile()) {
      throw new AwsException("The file: " + filePath + " doesn't exist.");
    }

    try {
      log.info("=====Uploading an object to S3 from a file start=====");
      s3Client.putObject(
          new PutObjectRequest(bucketName, path, file).withCannedAcl(accessType.getAccessLevel()));
      log.info("=====Uploading an object to S3 from a file end  =====");

    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3,"
              + " but was rejected with an error response for some reason.");
      log.error(String.format(ERROR_MESSAGE_FORMAT, ase.getMessage()));
      log.error(String.format("HTTP Status Code: %s", ase.getStatusCode()));
      log.error(String.format("AWS Error Code:   %s", ase.getErrorCode()));
      log.error(String.format("Error Type:       %s", ase.getErrorType()));
      log.error(String.format("Request ID:       %s", ase.getRequestId()));
      throw new AwsException(FAILED_TO_UPLOAD_FILE);
    } catch (final AmazonClientException ace) {
      logAmazonClientException(ace);
      throw new AwsException(FAILED_TO_UPLOAD_FILE);
    }

    deleteFile(file);
    return path;
  }

  private String findSavePathInAws(final String fileName, final Type type) {
    final Calendar now = Calendar.getInstance();
    final String year = String.valueOf(now.get(Calendar.YEAR));
    final String month = String.valueOf(now.get(Calendar.MONTH) + 1);
    final String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

    return String.format("%s/%s/%s/%s/%s/%s", folder, type.getFolder(), year, month, day, fileName);
  }

  public String moveFileFromTemp(final String path) {
    final AmazonS3 amazonS3 = findClient();
    try {
      final String key = path.replace(TEMP_URL, UPLOADS);
      log.info("move file from /temp");
      amazonS3.copyObject(new CopyObjectRequest(bucketName, path, bucketName, key));
      amazonS3.deleteObject(bucketName, path);
      return key;
    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but was"
              + " rejected with an error response for some reason.");
      log.error(String.format(ERROR_MESSAGE_FORMAT, ase.getMessage()));
      throw new AwsException(ase.getMessage());
    } catch (final AmazonClientException ace) {
      logAmazonClientException(ace);
      throw new AwsException(ace.getMessage());
    }
  }

  public void deleteFile(final File... files) {
    for (final File file : files) {
      try {
        Files.deleteIfExists(file.toPath());
      } catch (final IOException e) {
        log.error(String.format("Caught an IOException: %s", e.getMessage()));
        throw new AwsException(e.getMessage());
      }
    }
  }

  public void deleteFile(final String path) {
    if (path == null) {
      return;
    }
    final AmazonS3 amazonS3 = findClient();
    try {
      log.info("delete file from Amazon S3");
      amazonS3.deleteObject(bucketName, path);
    } catch (final AmazonServiceException ase) {
      log.error(
          "Caught an AmazonServiceException, which means your request made it to Amazon S3, but "
              + "was rejected with an error response for some reason.");
      log.error(String.format(ERROR_MESSAGE_FORMAT, ase.getMessage()));
      throw new AwsException(ase.getMessage());
    } catch (final AmazonClientException ace) {
      logAmazonClientException(ace);
      throw new AwsException(ace.getMessage());
    }
  }

  public String findPreSignedUrl(final String key) {
    try {
      final AmazonS3 s3Client = findClient();

      // Set the pre-signed URL to expire after one hour.
      final java.util.Date expiration = new java.util.Date();
      final long expTimeMillis = expiration.getTime() + EXPIRES;
      expiration.setTime(expTimeMillis);

      // Generate the pre-signed URL.
      final GeneratePresignedUrlRequest generatePresignedUrlRequest =
          new GeneratePresignedUrlRequest(bucketName, key)
              .withMethod(HttpMethod.GET)
              .withExpiration(expiration);
      final URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

      return url.toString();
    } catch (final SdkClientException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      throw new AwsException("Failed to get url of file.", ErrorType.AWS_GET_URL_EXCEPTION, e);
    }
  }

  private void logAmazonClientException(final AmazonClientException e) {
    log.error(
        "Caught an AmazonClientException, which means the client encountered an internal error "
            + "while trying to communicate with S3, "
            + "such as not being able to access the network.");
    log.error(String.format("Error Message: %s", e.getMessage()));
  }

  public String findAwsPath() {
    return "https://" + bucketName + ".s3.amazonaws.com/";
  }

  public String findFullFileUrl(final String path) {
    return findAwsPath() + path;
  }
}
