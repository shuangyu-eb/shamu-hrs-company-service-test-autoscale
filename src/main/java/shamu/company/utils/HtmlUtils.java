package shamu.company.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public abstract class HtmlUtils {

  private HtmlUtils() {}

  private static final PolicyFactory IMAGES =
      (new HtmlPolicyBuilder())
          .allowUrlProtocols("http", "https", "data")
          .allowElements("img")
          .allowAttributes("alt", "src")
          .onElements("img")
          .toFactory();

  // add 'target' attributes which allows open link in a new window
  private static final PolicyFactory LINKS =
      (new HtmlPolicyBuilder())
          .allowStandardUrlProtocols()
          .allowElements("a")
          .allowAttributes("href", "target")
          .onElements("a")
          .requireRelNofollowOnLinks()
          .toFactory();

  /*
  Referenced from:
  https://static.javadoc.io/com.googlecode.owasp-java-html-sanitizer/owasp-java-html-sanitizer/20190325.1/org/owasp/html/Sanitizers.html
  */
  public static String filterWelcomeMessage(String message) {
    final PolicyFactory policy =
        Sanitizers.FORMATTING
            .and(LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.TABLES)
            .and(IMAGES)
            .and(Sanitizers.STYLES);
    return policy.sanitize(message);
  }

}

