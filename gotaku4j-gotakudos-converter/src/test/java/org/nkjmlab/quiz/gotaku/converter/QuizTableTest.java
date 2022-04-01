package org.nkjmlab.quiz.gotaku.converter;

import static org.assertj.core.api.Assertions.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuizBook;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.result.RowMap;
import org.nkjmlab.util.java.lang.ResourceUtils;

class QuizTableTest {


  @Test
  void test() {
    File _5tqDir = ResourceUtils.getResourceAsFile("/quizbooks/5tq/正統派クイズ2000題");
    GotakuQuizBook book = new GotakuFileConverter().parse(_5tqDir);

    DataSource dataSorce = Sorm.createDataSource("jdbc:h2:mem:gotaku;DB_CLOSE_DELAY=-1", "sa", "");
    QuizzesTable quizTable = new QuizzesTable(dataSorce);
    quizTable.mergeBook(book);

    List<QuizzesTable.Quiz> quizzes = quizTable.selectAll();
    assertThat(quizzes.size()).isEqualTo(1794);

    List<RowMap> genreNames = quizTable.readGenres("正統派クイズ2000題");
    assertThat(genreNames.stream().map(m -> m.get("GENRE").toString()).collect(Collectors.toSet()))
        .isEqualTo(
            Set.of("ＴＶ・芸能・音楽", "歴史", "科学・工学・数学", "流行・文化・芸術", "スポーツ", "文学・語学", "地理・政経・時事", "雑学"));
  }


}
