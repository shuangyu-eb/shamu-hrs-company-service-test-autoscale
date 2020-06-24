package shamu.company.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.utils.FileValidateUtils.FileFormat;
import shamu.company.utils.exception.errormapping.FileFormatNotAcceptableException;
import shamu.company.utils.exception.errormapping.FileSizeExceedException;
import shamu.company.utils.exception.errormapping.FileSizeTooSmallException;

public class FileValidateUtilsTests {

  private static final String filePath = "src/test/resources/fixtures/files/";

  private static final File validImage = new File(filePath + "realImage.jpeg");

  @Nested
  class TestValidate {
    @Test
    void whenRenamedFromTxt() {
      final File fakeImage = new File(filePath + "renamedFromTxt.jpg");
      assertThatThrownBy(
              () ->
                  FileValidateUtils.validate(fakeImage, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .isInstanceOf(FileFormatNotAcceptableException.class);
    }

    @Test
    void whenValidImage() {
      assertThatCode(
              () ->
                  FileValidateUtils.validate(validImage, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .doesNotThrowAnyException();
    }

    @Test
    void whenNonExistingFile() {
      final File file = new File("nonExistingFile");
      assertThatThrownBy(
              () -> FileValidateUtils.validate(file, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .isInstanceOf(FileFormatNotAcceptableException.class);
    }

    @Test
    void whenExceedSizeLimitation() {
      assertThatThrownBy(
              () ->
                  FileValidateUtils.validate(validImage, 2 * FileValidateUtils.KB, FileFormat.JPEG))
          .isInstanceOf(FileSizeExceedException.class);
    }

    @Test
    void whenFileSizeLimitationSmallerThan256B() {
      assertThatThrownBy(() -> FileValidateUtils.validate(validImage, 256L, FileFormat.JPEG))
          .isInstanceOf(FileSizeTooSmallException.class);
    }

    @Test
    void whenImageWithLimitedFormatPdf() {
      assertThatThrownBy(
              () ->
                  FileValidateUtils.validate(validImage, 2 * FileValidateUtils.MB, FileFormat.PDF))
          .isInstanceOf(FileFormatNotAcceptableException.class);
    }

    @Test
    void whenMultipartFileSuffixIsJPEG() throws IOException {
      final MultipartFile file =
          new MockMultipartFile(
              "validImage.jpeg", "validImage.jpeg", "", new FileInputStream(validImage));
      assertThatCode(
              () -> FileValidateUtils.validate(file, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .doesNotThrowAnyException();
    }

    @Test
    void whenMultipartFileSuffixIsJPG() throws IOException {
      final MultipartFile file =
          new MockMultipartFile(
              "validImage.jpeg", "validImage.jpg", "", new FileInputStream(validImage));
      assertThatCode(
              () -> FileValidateUtils.validate(file, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .doesNotThrowAnyException();
    }

    @Test
    void whenMultipartFileNameIsInvalid() throws IOException {
      final MultipartFile file =
          new MockMultipartFile(
              "validImage.jpeg", "validImage.aaa", "", new FileInputStream(validImage));
      assertThatThrownBy(
              () -> FileValidateUtils.validate(file, 2 * FileValidateUtils.MB, FileFormat.JPEG))
          .isInstanceOf(FileFormatNotAcceptableException.class);
    }
  }
}
