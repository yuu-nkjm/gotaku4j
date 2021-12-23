package org.nkjmlab.quiz.gotaku.webui;

import static org.nkjmlab.sorm4j.table.TableSchema.Keyword.*;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.QuizRecordsTable.QuizRecord;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.table.Table;
import org.nkjmlab.sorm4j.table.TableSchema;

public class QuizRecordsTable implements Table<QuizRecord> {
  private static final String TABLE_NAME = "QUIZ_RECORDS";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String TOTAL_QUIZ_NUMBER = "total_quiz_number";
  private static final String TOTAL_SCORE = "total_score";
  private static final String TOTAL_CORRECT_ANSWERS = "total_correct_answers";

  private Sorm sorm;
  private TableSchema schema;

  public QuizRecordsTable(DataSource dataSorce) {
    this.sorm = Sorm.create(dataSorce);
    this.schema =
        TableSchema.builder(TABLE_NAME).addColumnDefinition(ID, INT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(NAME, VARCHAR).addColumnDefinition(TOTAL_QUIZ_NUMBER, INT)
            .addColumnDefinition(TOTAL_CORRECT_ANSWERS, INT).addColumnDefinition(TOTAL_SCORE, INT)
            .build();

  }


  public static class QuizRecord {
    public int id;
    public String name;
    public int totalQuizNumber;
    public int totalCorrectAnswers;
    public int totalScore;

    public QuizRecord(String name, int totalQuizNumber, int totalCorrectAnswers, int totalScore) {
      super();
      this.name = name;
      this.totalQuizNumber = totalQuizNumber;
      this.totalCorrectAnswers = totalCorrectAnswers;
      this.totalScore = totalScore;
    }

  }

  @Override
  public TableSchema getTableSchema() {
    return schema;
  }

  @Override
  public Class<QuizRecord> getValueType() {
    return QuizRecord.class;
  }

  @Override
  public Sorm getSorm() {
    return sorm;
  }

}
