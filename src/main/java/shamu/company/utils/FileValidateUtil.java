package shamu.company.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.exception.FileValidateException;

@Slf4j
public abstract class FileValidateUtil {

  public static final Long KB = 1024L;

  public static final Long MB = 1024 * KB;

  private FileValidateUtil() {}

  private static String bytesToHexString(final Byte[] src) {
    final StringBuilder stringBuilder = new StringBuilder();
    if (src == null || src.length <= 0) {
      return null;
    }

    Stream.of(src)
        .forEach(
            chars -> {
              final int v = chars & 0xFF;
              final String hv = Integer.toHexString(v);
              if (hv.length() < 2) {
                stringBuilder.append(0);
              }
              stringBuilder.append(hv);
            });
    return stringBuilder.toString();
  }

  private static String getFileContent(final InputStream is) throws IOException {
    final byte[] b = new byte[28];
    is.read(b, 0, 28);
    return bytesToHexString(ArrayUtils.toObject(b));
  }

  private static FileType getType(final InputStream is) throws IOException {
    String fileHead = getFileContent(is);
    if (fileHead == null || fileHead.length() == 0) {
      return null;
    }
    fileHead = fileHead.toUpperCase();
    final FileType[] fileTypes = FileType.values();
    for (final FileType type : fileTypes) {
      if (fileHead.startsWith(type.getValue())) {
        return type;
      }
    }
    throw new FileValidateException("File type error.");
  }

  public static void validate(
      final MultipartFile multipartFile, final Long maxSize, final FileType... types) {
    if (multipartFile == null || multipartFile.isEmpty()) {
      throw new FileValidateException("No file was found.");
    }

    try (final InputStream inputStream = multipartFile.getInputStream()) {
      validate(inputStream, multipartFile.getSize(), maxSize, types);
    } catch (final IOException e) {
      log.error(String.format("Caught an exception while validate file: %s", e.getMessage()));
      throw new FileValidateException("Failed to upload file!");
    }
  }

  public static void validate(final File file, final Long maxSize, final FileType... types) {
    if (file == null || !file.isFile()) {
      throw new FileValidateException("No file was found.");
    }

    try {
      validate(new FileInputStream(file), file.length(), maxSize, types);
    } catch (final FileNotFoundException e) {
      throw new FileValidateException("File not found.");
    } catch (final IOException e) {
      log.error(String.format("Caught an exception while uploading file: %s", e.getMessage()));
      throw new FileValidateException("Failed to upload file!");
    }
  }

  private static void validate(
      final InputStream inputStream,
      final Long fileSize,
      final Long maxSize,
      final FileType... types)
      throws IOException {
    final FileType fileType = getType(inputStream);

    if (Arrays.asList(types).indexOf(fileType) == -1) {
      throw new FileValidateException("File type error.");
    }

    if (fileSize > maxSize) {
      throw new FileValidateException(String.format("File size exceeds limit: %d.", maxSize));
    }
  }

  public enum FileType {
    /** JEPG. */
    JPEG("FFD8FF"),

    /** PNG. */
    PNG("89504E47"),

    /** GIF. */
    GIF("47494638"),

    /** Adobe Acrobat. */
    PDF("255044462D312E");

    private String value = "";

    FileType(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
