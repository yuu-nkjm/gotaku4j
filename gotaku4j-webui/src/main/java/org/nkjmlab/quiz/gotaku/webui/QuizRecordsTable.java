package org.nkjmlab.quiz.gotaku.webui;

import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.QuizRecordsTable.QuizRecord;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.BasicTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;
import org.nkjmlab.sorm4j.util.table_def.annotation.AutoIncrement;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKey;

public class QuizRecordsTable extends BasicTableWithDefinition<QuizRecord> {

  public QuizRecordsTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), QuizRecord.class,
        TableDefinition.builder(QuizRecord.class).build());
    createTableIfNotExists();
    createIndexesIfNotExists();

  }


  public record QuizRecord(@PrimaryKey @AutoIncrement long id, String name, int totalQuizNumber,
      int totalCorrectAnswers, int totalScore) {
  }


}
