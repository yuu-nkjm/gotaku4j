package org.nkjmlab.quiz.gotaku.util;

public class HexUtils {
  private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();

  public static String toHexString(byte[] bytes) {
    StringBuilder r = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      r.append(HEX_CODE[(b >> 4) & 0xF]);
      r.append(HEX_CODE[(b & 0xF)]);
    }
    return r.toString();
  }

}
