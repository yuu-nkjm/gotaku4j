package org.nkjmlab.quiz.gotaku.webui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.QuizRecordsTable.QuizRecord;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.Index;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyColumns;
import org.nkjmlab.util.java.time.DateTimeUtils;

public class QuizRecordsTable extends BasicH2Table<QuizRecord> {

  public QuizRecordsTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), QuizRecord.class);
    createTableIfNotExists();
    createIndexesIfNotExists();
  }

  @OrmRecord
  @PrimaryKeyColumns({"player_id", "game_id"})
  public static record QuizRecord(String playerId, long gameId, @Index String bookName, int stage,
      int totalQuizNumber, int totalCorrectAnswers, int totalScore, LocalDateTime createdAt) {
  }

  public List<Object[]> readScoreRanking(String bookName) {
    List<QuizRecord> records =
        readList("select * from QUIZ_RECORDS where book_name=? order by total_score desc limit 10",
            bookName);
    return toObjectList(records);
  }

  public List<Object[]> readAccuracyRateRanking(String bookName) {
    List<QuizRecord> records = readList(
        "select *, CAST(total_correct_answers AS DOUBLE)/total_quiz_number AS accuracy_rate from QUIZ_RECORDS where book_name=? order by accuracy_rate desc limit 10",
        bookName);
    return toObjectList(records);
  }

  private List<Object[]> toObjectList(List<QuizRecord> records) {
    List<Object[]> ret = new ArrayList<>();
    for (int i = 0; i < records.size(); i++) {
      QuizRecord r = records.get(i);
      ret.add(
          new Object[] {i + 1, r.playerId, r.totalScore,
              String.format("%.2f", ((double) r.totalCorrectAnswers * 100 / r.totalQuizNumber))
                  + "%",
              r.stage,
              DateTimeUtils.GOOGLE_SPREADSHEET_DEFAULT_DATE_TIME_FORMATTER.format(r.createdAt())});
    }
    return ret;
  }



}
