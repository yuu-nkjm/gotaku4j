package org.nkjmlab.quiz.gotaku.webui;

import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.QuizResponsesTable.QuizResponse;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.result.RowMap;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.IndexColumns;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyColumns;

public class QuizResponsesTable extends BasicH2Table<QuizResponse> {

  public QuizResponsesTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), QuizResponse.class);
    createTableIfNotExists();
    createIndexesIfNotExists();

  }


  @PrimaryKeyColumns({"player_id", "game_id", "stage", "q_num"})
  @IndexColumns({"player_id", "book_name", "jadge"})
  @IndexColumns({"player_id", "book_name"})
  public static record QuizResponse(String playerId, long gameId, int stage, int qNum,
      String bookName, int qid, int elapsedTime, String choice, boolean jadge,
      LocalDateTime createdAt) {
  }


  public List<RowMap> readPlayerLog(String playerId, String bookName, String genre) {
    String sql =
        """
        WITH
            R AS (SELECT * FROM QUIZ_RESPONSES where PLAYER_ID=?),
            Q AS(SELECT * FROM QUIZS where BOOK_NAME=? AND GENRE=?),
            R1 AS(
            SELECT Q.BOOK_NAME, Q.QID, SUM(CASE WHEN JADGE=TRUE THEN 1 ELSE -1 END) AS SCORE , COUNT(*) AS NUM
            FROM Q
            LEFT JOIN R
            USING (BOOK_NAME, QID)  GROUP BY Q.BOOK_NAME, Q.QID ORDER BY SCORE DESC)
            SELECT R1.BOOK_NAME ,R1.QID ,QUESTION ,CHOICE1 ,EXPLANATION, SCORE  FROM R1 JOIN QUIZS USING(BOOK_NAME,QID)
                    """;

    return getOrm().readList(RowMap.class, sql, playerId, bookName, genre);
  }


}
