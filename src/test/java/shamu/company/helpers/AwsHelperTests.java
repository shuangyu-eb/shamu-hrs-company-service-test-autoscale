package shamu.company.helpers;

import com.amazonaws.AmazonClientException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.exception.AwsException;
import shamu.company.helpers.s3.AccessType;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.helpers.s3.Type;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



public class AwsHelperTests {

  private AwsHelper awsHelper = new AwsHelper("simplyhired-hrs-eastbay-dev-company","media-local");

  @BeforeEach
  void initial() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class AmazonS3DeleteObject {

    List<String> listKey;
    @BeforeEach
    void init() {
      listKey = new ArrayList<>(2);
    }

    @Test
    void whenKeyIsEmpty_thenDoNothing() {
      Assertions.assertDoesNotThrow(() -> awsHelper.amazonS3DeleteObject(listKey));
    }

    @Test
    void whenKeyIsNotEmpty_thenShouldSuccess() {
      listKey.add("1");
      listKey.add("2");
      Assertions.assertDoesNotThrow(() -> awsHelper.amazonS3DeleteObject(listKey));
    }
  }

  @Nested
  class UploadFileWithFile {

    MultipartFile multipartFile;

    @BeforeEach
    void init() {
      multipartFile = Mockito.mock(MultipartFile.class);
      Mockito.when(multipartFile.getOriginalFilename()).thenReturn("file.jpg");
    }

    @Test
    void whenFileIsEmpty_thenShouldThrow() {
      Mockito.when(multipartFile.isEmpty()).thenReturn(true);
      Assertions.assertThrows(
          AwsException.class,
          () -> awsHelper.uploadFile(multipartFile)
      );
    }

    @Test
    void testUploadWithOnlyFile() {
      Assertions.assertDoesNotThrow(() -> awsHelper.uploadFile(multipartFile));
    }

    @Test
    void testUploadWithFileAndAccessType() {
      Assertions.assertDoesNotThrow(() -> awsHelper.uploadFile(multipartFile, AccessType.valueOf("PRIVATE")));
    }

    @Test
    void whenUploadWithFileAndTypeS3Error_thenShouldThrow() {
      final AwsHelper falseAwsHelper = new AwsHelper("1","1");
      Assertions.assertThrows(
          AwsException.class,
          () -> falseAwsHelper.uploadFile(multipartFile, Type.IMAGE)
      );
    }

    @Test
    void whenUploadWithFileAndTypeS3Right_thenShouldSuccess() {
      Assertions.assertDoesNotThrow(() -> awsHelper.uploadFile(multipartFile, Type.IMAGE));
    }
  }

  @Nested
  class UploadFileWithPath {

    @Test
    void whenFileNotExist_thenShouldThrow() {
      Assertions.assertThrows(
          AwsException.class,
          () -> awsHelper.uploadFile("default/file.jpg", Type.IMAGE)
      );
    }
  }

  @Nested
  class MoveFileFromTemp {

    @Test
    void whenS3BucketRight_thenShouldSuccess() {
      Assertions.assertThrows(
          AwsException.class,
          () -> awsHelper.moveFileFromTemp("default/file.jpg")
      );
    }
  }

  @Nested
  class DeleteFile {

    @Test
    void whenFileError_thenShouldThrow() {
      File file = Mockito.mock(File.class);
      Path path = Mockito.mock(Path.class);
      Mockito.when(file.toPath()).thenReturn(path);
      Assertions.assertThrows(
          AwsException.class,
          () -> awsHelper.deleteFile(new File("/"))
      );
    }

    @Test
    void whenFileNoError_thenShouldSuccess() {
      File file = Mockito.mock(File.class);
      Path path = Mockito.mock(Path.class);
      Mockito.when(file.toPath()).thenReturn(path);
      Assertions.assertDoesNotThrow(() -> awsHelper.deleteFile(new File("1")));
    }

  }

  @Nested
  class DeleteFileWithPath {

    @Test
    void whenS3BucketWrong_thenShouldThrow() {
      final AwsHelper falseAwsHelper = new AwsHelper("1","1");
      Assertions.assertThrows(
          AwsException.class,
          () -> falseAwsHelper.deleteFile("default/file.jpg"));
    }

    @Test
    void whenS3BucketRight_thenShouldSuccess() {
      Assertions.assertDoesNotThrow(() -> awsHelper.deleteFile("default/file.jpg"));
    }
  }



  @Test
  void testFindPreSignedUrl() {
    Assertions.assertDoesNotThrow(() -> awsHelper.findPreSignedUrl("1"));
  }

  @Test
  void testFindAwsPath() {
    Assertions.assertNotNull(awsHelper.findAwsPath());
  }

  @Test
  void testFindFullFileUrl() {
    Assertions.assertNotNull(awsHelper.findFullFileUrl("default/file.jpg"));
  }

  @Test
  void testLogAmazonClientException() throws Exception {
    Whitebox.invokeMethod(awsHelper, "logAmazonClientException", new AmazonClientException("1"));
  }

}
