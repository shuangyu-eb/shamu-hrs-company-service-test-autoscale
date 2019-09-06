package shamu.company.utils;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shamu.company.common.exception.FileValidateException;
import shamu.company.utils.FileValidateUtil.FileType;

public class FileValidateUtilTests {

  private static final String filePath = "src/test/resources/testFiles/";

  @Test
  void testModifiedTheSuffixTxt() {
    final File fakeImage = new File(filePath + "renameFromTxt.jpg");
    Assertions.assertThrows(FileValidateException.class, () -> {
      FileValidateUtil.validate(fakeImage, 2 * FileValidateUtil.MB, FileType.JPEG);
    });
  }

  @Test
  void testRealImage() {
    final File image = new File(filePath + "realImage.jpeg");
    Assertions.assertDoesNotThrow(() -> {
      FileValidateUtil.validate(image, 2 * FileValidateUtil.MB, FileType.JPEG);
    });
  }

  @Test
  void testFileSizeValidation() {
    final File image = new File(filePath + "realImage.jpeg");
    Assertions.assertThrows(FileValidateException.class, () -> {
      FileValidateUtil.validate(image, 2 * FileValidateUtil.KB, FileType.JPEG);
    });

    Assertions.assertDoesNotThrow(() -> {
      FileValidateUtil.validate(image, 2 * FileValidateUtil.MB, FileType.JPEG);
    });
  }
}
