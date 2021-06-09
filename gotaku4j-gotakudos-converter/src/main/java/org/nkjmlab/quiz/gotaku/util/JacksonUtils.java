package org.nkjmlab.quiz.gotaku.util;

import java.io.File;
import org.nkjmlab.sorm4j.internal.util.Try;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtils {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static <T> T readValue(String text, Class<T> clazz) {
    return Try.getOrThrow(() -> mapper.readValue(text, clazz), Try::rethrow);
  }

  public static String writeValue(Object object) {
    return Try.getOrThrow(() -> mapper.writeValueAsString(object), Try::rethrow);
  }

  public static <T> T readValue(File file, Class<T> clazz) {
    return Try.getOrThrow(() -> mapper.readValue(file, clazz), Try::rethrow);
  }

  public static <T> T readValue(File file, TypeReference<T> typeReference) {
    return Try.getOrThrow(() -> mapper.readValue(file, typeReference), Try::rethrow);
  }


}
