package org.nkjmlab.quiz.gotaku.webui;

import static org.nkjmlab.sorm4j.sql.schema.TableSchemaKeyword.*;
import javax.sql.DataSource;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.sql.schema.TableSchema;

public class RecordsTable {
  private static final String TABLE_NAME = "RECORDS";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String TOTAL_QUIZ_NUMBER = "total_quiz_number";
  private static final String TOTAL_SCORE = "total_score";
  private static final String TOTAL_CORRECT_ANSWERS = "total_correct_answers";

  private Sorm sorm;
  private TableSchema schema;

  public RecordsTable(DataSource dataSorce) {
    this.sorm = Sorm.create(dataSorce);
    this.schema =
        TableSchema.builder(TABLE_NAME).addColumnDefinition(ID, INT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(NAME, VARCHAR).addColumnDefinition(TOTAL_QUIZ_NUMBER, INT)
            .addColumnDefinition(TOTAL_CORRECT_ANSWERS, INT).addColumnDefinition(TOTAL_SCORE, INT)
            .build();

  }

  void insert(String name, int totalQuizNumber, int totalCorrectAnswers, int totalScore) {
    sorm.insert(new Record(name, totalQuizNumber, totalCorrectAnswers, totalScore));
  }

  public static class Record {
    public int id;
    public String name;
    public int totalQuizNumber;
    public int totalCorrectAnswers;
    public int totalScore;

    public Record(String name, int totalQuizNumber, int totalCorrectAnswers, int totalScore) {
      super();
      this.name = name;
      this.totalQuizNumber = totalQuizNumber;
      this.totalCorrectAnswers = totalCorrectAnswers;
      this.totalScore = totalScore;
    }

  }

}
