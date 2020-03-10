package shamu.company.tests.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * This class is meant to generate the rendered email content, so that you can tune the style in browsers
 */
public class ThymeleafUtil {


  private static String getContent(final String emailTemplate, final Context context) {
    final TemplateEngine templateEngine = new TemplateEngine();
    final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("/templates/");
    resolver.setSuffix(".html");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setTemplateMode(TemplateMode.HTML);
    templateEngine.setTemplateResolver(resolver);

    return templateEngine.process(emailTemplate, context);
  }

  public static void generateEmail(final String templateName, final Context context) throws IOException {
    final String emailContent = getContent(templateName, context);

    final String directory = "target/emails/";
    final File fileDirectory = new File(directory);
    if (!fileDirectory.exists() && !fileDirectory.mkdir()) {
      throw new IOException("Can not create email directory.");
    }

    final String path = directory + templateName;

    final File file = new File(path);
    if (!file.exists() && !file.createNewFile()) {
      throw new IOException("Can not create target file.");
    }

    final Path filePath = Paths.get(path);
    Files.write(filePath, emailContent.getBytes(StandardCharsets.UTF_8));
  }
}
