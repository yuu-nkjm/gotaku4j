package org.nkjmlab.quiz.gotaku.converter;

import static org.assertj.core.api.Assertions.*;
import java.io.File;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.util.java.lang.ResourceUtils;

class GotakuCsvConverterTest {

  @Test
  void testParseAll() {
    DataSource dataSorce = Sorm.createDataSource("jdbc:h2:mem:gotaku;DB_CLOSE_DELAY=-1", "sa", "");
    QuizzesTable quizTable = new QuizzesTable(dataSorce);
    File _5tqsDir = ResourceUtils.getResourceAsFile("/quizbooks/5tqcsv");
    new GotakuCsvConverter(quizTable).parseAll(_5tqsDir);
    System.out.println(quizTable.selectAll());
  }


  @Test
  void url() {
    String text =
        "加曽利貝塚[https://i.gyazo.com/c8.jpg] <img src=\"https://i.gyazo.com/d0.jp\"> <img src='https://i.gyazo.com/d4.jpg'>";
    String ret = GotakuCsvConverter.convertImageUrlToImageTag(text);
    assertThat(ret).isEqualTo(
        "加曽利貝塚<img class='img-thumbnail' style='max-width:400px' src='https://i.gyazo.com/c8.jpg'> <img src=\"https://i.gyazo.com/d0.jp\"> <img src='https://i.gyazo.com/d4.jpg'>");

  }

}
