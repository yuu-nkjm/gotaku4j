package org.nkjmlab.quiz.gotaku.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.nkjmlab.sorm4j.internal.util.Try;

public class ProcessUtils {

  public static boolean isWindowsOs() {
    return System.getProperty("os.name").toLowerCase().indexOf("windows") > -1;
  }

  public static boolean isJapaneseOs() {
    return Locale.getDefault() == Locale.JAPANESE || Locale.getDefault() == Locale.JAPAN;
  }


  /**
   * Reads read standard output after process finish.
   *
   * @param process
   * @return
   */
  private static String readStandardOutputAfterProcessFinish(Process process) {
    try {
      byte[] b = process.getInputStream().readAllBytes();
      return new String(b,
          isWindowsOs() && isJapaneseOs() ? "MS932" : StandardCharsets.UTF_8.toString());
    } catch (IOException e) {
      throw Try.rethrow(e);
    }
  }

  public static Optional<String> getProcessIdBidingPort(int port) {
    try {
      List<String> command = isWindowsOs() ? List.of("cmd", "/c", "netstat", "-ano")
          : List.of("lsof", "-i", ":" + port);

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String lines = readStandardOutputAfterProcessFinish(proc);

      if (isWindowsOs()) {
        return Arrays.stream(lines.split(System.lineSeparator()))
            .filter(l -> l.contains(":" + port + " ")).findAny().map(l -> {
              String[] t = l.split("\\s");
              return t[t.length - 1];
            });
      } else {
        return Arrays.stream(lines.split(System.lineSeparator())).findAny()
            .map(l -> l.split("\\s")[1]);
      }
    } catch (Exception e) {
      throw Try.rethrow(e);
    }
  }

  public static boolean stopProcessBindingPortIfExists(int port) {
    return getProcessIdBidingPort(port).map(pid -> {
      // log.info("existing pid = [{}]", pid);
      try {
        List<String> command =
            isWindowsOs() ? List.of("taskkill", "/F", "/T", "/PID", pid) : List.of("kill", pid);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.start().waitFor();
      } catch (InterruptedException | IOException e) {
        throw Try.rethrow(e);
      }
      // log.info("Success to stop the process [{}] biding port :[{}].", pid, port);
      return true;
    }).orElse(false);
  }

}
