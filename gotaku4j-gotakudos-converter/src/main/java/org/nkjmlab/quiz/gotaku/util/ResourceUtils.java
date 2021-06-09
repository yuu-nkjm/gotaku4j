package org.nkjmlab.quiz.gotaku.util;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ResourceUtils {

  public static File getFile(String file) {
    return new File(
        Try.getOrThrow(() -> ResourceUtils.class.getResource(file).toURI(), Try::rethrow));
  }

  public static List<String> getFileAndReadAllLines(String fileName) {
    return Try.getOrThrow(() -> Files.readAllLines(getFile(fileName).toPath()), Try::rethrow);
  }

}
