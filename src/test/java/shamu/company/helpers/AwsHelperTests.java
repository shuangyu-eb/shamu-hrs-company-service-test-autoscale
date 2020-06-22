package shamu.company.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.amazonaws.AmazonClientException;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.helpers.exception.errormapping.FileDeletionFailedException;
import shamu.company.helpers.exception.errormapping.FileMovementFailedException;
import shamu.company.helpers.exception.errormapping.FileUploadFailedException;
import shamu.company.helpers.s3.AccessType;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.helpers.s3.Type;
import shamu.company.timeoff.exception.NotFoundException;

public class AwsHelperTests {

  private final AwsHelper awsHelper =
      new AwsHelper("simplyhired-hrs-eastbay-dev-company", "media-local");

  @BeforeEach
  void initial() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testFindPreSignedUrl() {
    assertThatCode(() -> awsHelper.findPreSignedUrl("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindAwsPath() {
    assertThat(awsHelper.findAwsPath()).isNotNull();
  }

  @Test
  void testFindFullFileUrl() {
    assertThat(awsHelper.findFullFileUrl("default/file.jpg")).isNotNull();
  }

  @Test
  void testLogAmazonClientException() throws Exception {
    Whitebox.invokeMethod(awsHelper, "logAmazonClientException", new AmazonClientException("1"));
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
      assertThatCode(() -> awsHelper.amazonS3DeleteObject(listKey)).doesNotThrowAnyException();
    }

    @Test
    void whenKeyIsNotEmpty_thenShouldSuccess() {
      listKey.add("1");
      listKey.add("2");
      assertThatCode(() -> awsHelper.amazonS3DeleteObject(listKey)).doesNotThrowAnyException();
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
      assertThatExceptionOfType(NotFoundException.class)
          .isThrownBy(() -> awsHelper.uploadFile(multipartFile));
    }

    @Test
    void testUploadWithOnlyFile() {
      assertThatCode(() -> awsHelper.uploadFile(multipartFile)).doesNotThrowAnyException();
    }

    @Test
    void testUploadWithFileAndAccessType() {
      assertThatCode(() -> awsHelper.uploadFile(multipartFile, AccessType.valueOf("PRIVATE")))
          .doesNotThrowAnyException();
    }

    @Test
    void whenUploadWithFileAndTypeS3Error_thenShouldThrow() {
      final AwsHelper falseAwsHelper = new AwsHelper("1", "1");
      assertThatExceptionOfType(FileUploadFailedException.class)
          .isThrownBy(() -> falseAwsHelper.uploadFile(multipartFile, Type.IMAGE));
    }

    @Test
    void whenUploadWithFileAndTypeS3Right_thenShouldSuccess() {
      assertThatCode(() -> awsHelper.uploadFile(multipartFile, Type.IMAGE))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class UploadFileWithPath {

    @Test
    void whenFileNotExist_thenShouldThrow() {
      assertThatExceptionOfType(NotFoundException.class)
          .isThrownBy(() -> awsHelper.uploadFile("default/file.jpg", Type.IMAGE));
    }
  }

  @Nested
  class MoveFileFromTemp {

    @Test
    void whenS3BucketRight_thenShouldSuccess() {
      assertThatExceptionOfType(FileMovementFailedException.class)
          .isThrownBy(() -> awsHelper.moveFileFromTemp("default/file.jpg"));
    }
  }

  @Nested
  class DeleteFile {

    @Test
    void whenFileError_thenShouldThrow() {
      final File file = Mockito.mock(File.class);
      final Path path = Mockito.mock(Path.class);
      Mockito.when(file.toPath()).thenReturn(path);
      assertThatExceptionOfType(FileDeletionFailedException.class)
          .isThrownBy(() -> awsHelper.deleteFile(new File("/")));
    }

    @Test
    void whenFileNoError_thenShouldSuccess() {
      final File file = Mockito.mock(File.class);
      final Path path = Mockito.mock(Path.class);
      Mockito.when(file.toPath()).thenReturn(path);
      assertThatCode(() -> awsHelper.deleteFile(new File("1"))).doesNotThrowAnyException();
    }
  }

  @Nested
  class DeleteFileWithPath {

    @Test
    void whenS3BucketWrong_thenShouldThrow() {
      final AwsHelper falseAwsHelper = new AwsHelper("1", "1");
      assertThatExceptionOfType(FileDeletionFailedException.class)
          .isThrownBy(() -> falseAwsHelper.deleteFile("default/file.jpg"));
    }

    @Test
    void whenS3BucketRight_thenShouldSuccess() {
      assertThatCode(() -> awsHelper.deleteFile("default/file.jpg")).doesNotThrowAnyException();
    }
  }
}
