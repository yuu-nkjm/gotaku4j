package org.nkjmlab.quiz.gotaku.gotakudos;

import static org.assertj.core.api.Assertions.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.nkjmlab.quiz.gotaku.util.ResourceUtils;
import org.nkjmlab.sorm4j.Sorm;

class QuizTableTest {

  @Test
  void test() {
    File _5tqDir = ResourceUtils.getFile("/quizbooks/5tq/正統派クイズ2000題");
    GotakuQuizBook book = new GotakuFileConverter().parse(_5tqDir);

    DataSource dataSorce =
        Sorm.createDriverManagerDataSource("jdbc:h2:mem:gotaku;DB_CLOSE_DELAY=-1", "sa", "");
    QuizTable quizTable = new QuizTable(dataSorce);
    quizTable.createTableAndIndexesIfNotExists();
    quizTable.insert(book);

    List<QuizTable.Quiz> quizzes = quizTable.readAllQuizzes();
    assertThat(quizzes.size()).isEqualTo(1794);

    List<String> genreNames = quizTable.readAllGenreNames();
    assertThat(new HashSet<>(genreNames)).isEqualTo(
        Set.of("ＴＶ・芸能・音楽", "歴史", "科学・工学・数学", "流行・文化・芸術", "スポーツ", "文学・語学", "地理・政経・時事", "雑学"));
  }

}
