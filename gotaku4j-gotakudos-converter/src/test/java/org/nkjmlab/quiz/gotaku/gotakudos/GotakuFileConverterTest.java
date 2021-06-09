package org.nkjmlab.quiz.gotaku.gotakudos;

import static org.assertj.core.api.Assertions.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.nkjmlab.quiz.gotaku.util.ResourceUtils;

class GotakuFileConverterTest {

  @Test
  void testParseAll() {
    File _5tqsDir = ResourceUtils.getFile("/quizbooks/5tq");
    new GotakuFileConverter().parseAll(_5tqsDir);
  }

  @Test
  void testConvertToFile() {
    File _5tq = ResourceUtils.getFile("/quizbooks/5tq/中学校理科編(99年度3年生用)/RIKA3NEN.5TQ");
    File outputFile = ResourceUtils.getFile("/quizbooks/5tq/中学校理科編(99年度3年生用)/");

    new GotakuFileConverter().convertToTextFile(_5tq, outputFile);

  }

  @Test
  void testParseFile() {
    File _5tqDir = ResourceUtils.getFile("/quizbooks/5tq/正統派クイズ2000題");
    GotakuQuizBook book = new GotakuFileConverter().parse(_5tqDir);

    assertThat(book.getBookName()).isEqualTo("正統派クイズ2000題");
    assertThat(book.getGenres().stream().map(g -> g.getGenreName()).collect(Collectors.toList()))
        .isEqualTo(
            List.of("ＴＶ・芸能・音楽", "歴史", "科学・工学・数学", "流行・文化・芸術", "スポーツ", "文学・語学", "地理・政経・時事", "雑学"));


    assertThat(book.getGenres().stream().flatMap(g -> g.getQuizzes().stream()).count())
        .isEqualTo(1794);

    System.out.println(book);
  }

}
