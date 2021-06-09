package org.nkjmlab.quiz.gotaku.gotakudos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.nkjmlab.quiz.gotaku.util.Try;

public class GotakuTextFileConverter {

  public GotakuQuizBook parse(File _5tqTxtDir) {
    try {
      File tocFile = new File(_5tqTxtDir, "toc.txt");
      List<String> toc =
          tocFile.exists() ? Files.readAllLines(tocFile.toPath()) : Collections.emptyList();
      List<GotakuQuizGenre> sections = Arrays.stream(_5tqTxtDir.listFiles()).sorted()
          .filter(f -> f.isFile() && f.getName().matches("^[0-9]-.*")).map(f -> createQuizGenre(f))
          .collect(Collectors.toList());
      return new GotakuQuizBook(_5tqTxtDir.getName(), toc, sections);
    } catch (IOException e) {
      throw Try.rethrow(e);
    }
  }



  private GotakuQuizGenre createQuizGenre(File problemFile) {
    try {
      List<String> lines = Files.readAllLines(problemFile.toPath());
      String ganre = lines.get(0);
      List<GotakuQuiz> gotakuQuizs = createQuizzes(lines);
      return new GotakuQuizGenre(ganre, gotakuQuizs);
    } catch (IOException e) {
      System.err.println(problemFile.toString());
      throw Try.rethrow(e);
    }

  }

  private List<GotakuQuiz> createQuizzes(List<String> lines) {
    List<GotakuQuiz> gotakuQuizs = new ArrayList<>();
    int problemNum = lines.size() / 7;
    for (int pn = 0; pn < problemNum; pn++) {
      int offset = pn * 7;
      GotakuQuiz p = new GotakuQuiz(lines.get(offset + 1), lines.get(offset + 2),
          List.of(lines.get(offset + 2), lines.get(offset + 3), lines.get(offset + 4),
              lines.get(offset + 5), lines.get(offset + 6)));
      gotakuQuizs.add(p);
    }
    return gotakuQuizs;
  }


}
