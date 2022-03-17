package org.nkjmlab.quiz.gotaku.converter;

import java.io.File;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.nkjmlab.quiz.gotaku.model.GotakuCsvConverter;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.util.java.lang.ResourceUtils;

class GotakuCsvConverterTest {

  @Test
  void testParseAll() {
    File _5tqsDir = ResourceUtils.getResourceAsFile("/quizbooks/5tqcsv");
    DataSource dataSorce = Sorm.createDataSource("jdbc:h2:mem:gotaku;DB_CLOSE_DELAY=-1", "sa", "");
    QuizzesTable quizTable = new QuizzesTable(dataSorce);
    new GotakuCsvConverter(quizTable).parseAll(_5tqsDir);
  }


}
