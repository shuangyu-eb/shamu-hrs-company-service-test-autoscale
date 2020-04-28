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
public abstract class FileValidateUtils {

  public static final Long KB = 1024L;

  public static final Long MB = 1024L * KB;

  public static final String JPG = "JPG";

  public static void validate(final File file, final Long maxSize, final FileFormat... formats) {
    if (!file.isFile()) {
      throw new FileValidateException("File is invalid!");
    }

    try {
      validateWithInputStream(new FileInputStream(file), file.length(), maxSize, formats);
    } catch (final FileNotFoundException e) {
      throw new FileValidateException("File not found!");
    }
  }

  public static void validate(
      final MultipartFile multipartFile, final Long maxSize, final FileFormat... formats) {
    if (multipartFile.isEmpty()) {
      throw new FileValidateException("Multipart file not found!");
    }

    final boolean isValidatedFileName =
        validateWithFileName(multipartFile.getOriginalFilename(), formats);
    if (!isValidatedFileName) {
      throw new FileValidateException("File type error.");
    }

    try (final InputStream inputStream = multipartFile.getInputStream()) {
      validateWithInputStream(inputStream, multipartFile.getSize(), maxSize, formats);
    } catch (final IOException e) {
      log.error(String.format("Caught an exception while validate file: %s", e.getMessage()));
      throw new FileValidateException("Failed to upload file!");
    }
  }

  private static boolean validateWithFileName(final String fileName, final FileFormat... formats) {
    final String fileSuffix = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();

    for (final FileFormat format : formats) {
      if (format.name().equals(fileSuffix)) {
        return true;
      }
    }

    // The suffix of 'JPEG' file can be 'JPEG' or 'JPG'.
    return JPG.equals(fileSuffix);
  }

  private static void validateWithInputStream(
      final InputStream inputStream,
      final Long fileSize,
      final Long maxSize,
      final FileFormat... formats) {
    final FileFormat fileFormat = getFileFormat(inputStream);

    if (Arrays.asList(formats).indexOf(fileFormat) == -1) {
      throw new FileValidateException("File format not valid!");
    }

    if (fileSize > maxSize) {
      throw new FileValidateException(
          String.format("File size exceeds %s!", getHumanReadableFileSize(maxSize)));
    }
  }

  private static String getHumanReadableFileSize(final Long size) {
    if (size < KB) {
      throw new FileValidateException("Unsupported file size!");
    } else if (size < MB) {
      return size / KB + "KB";
    } else {
      return size / MB + "MB";
    }
  }

  private static FileFormat getFileFormat(final InputStream inputStream) {
    try {
      String fileHeader = readFileHeader(inputStream);
      fileHeader = fileHeader.toUpperCase();
      if (fileHeader.length() == 0) {
        throw new FileValidateException("File format not recognized!");
      }

      final FileFormat[] fileFormats = FileFormat.values();
      for (final FileFormat format : fileFormats) {
        if (fileHeader.startsWith(format.getSignature())) {
          return format;
        }
      }
    } catch (final IOException e) {
      log.error(String.format("Caught an IO exception while validate file: %s", e.getMessage()));
    }
    throw new FileValidateException("File format not recognized!");
  }

  private static String readFileHeader(final InputStream inputStream) throws IOException {
    final byte[] bytes = new byte[28];
    inputStream.read(bytes, 0, 28);
    return bytesToHexString(ArrayUtils.toObject(bytes));
  }

  private static String bytesToHexString(final Byte[] bytes) throws IOException {
    final StringBuilder stringBuilder = new StringBuilder();
    if (bytes.length <= 0) {
      throw new IOException("Byte array is empty!");
    }

    Stream.of(bytes)
        .forEach(
            chars -> {
              final int value = chars & 0xFF;
              final String hexValue = Integer.toHexString(value);
              if (hexValue.length() < 2) {
                stringBuilder.append(0);
              }
              stringBuilder.append(hexValue);
            });
    return stringBuilder.toString();
  }

  /**
   * The signature map to each format is stored in the file header, which could be used to identify
   * or verify the content of a file. See more here:
   * https://en.wikipedia.org/wiki/List_of_file_signatures
   */
  public enum FileFormat {
    JPEG("FFD8FF"),
    PNG("89504E47"),
    GIF("47494638"),
    PDF("255044462D");

    private String signature = "";

    FileFormat(final String signature) {
      this.signature = signature;
    }

    public String getSignature() {
      return signature;
    }
  }
}
