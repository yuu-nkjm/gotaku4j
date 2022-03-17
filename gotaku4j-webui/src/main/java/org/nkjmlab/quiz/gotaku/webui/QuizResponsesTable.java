package org.nkjmlab.quiz.gotaku.webui;

import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.QuizResponsesTable.QuizResponse;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.h2.BasicH2TableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.annotation.IndexPair;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyPair;

public class QuizResponsesTable extends BasicH2TableWithDefinition<QuizResponse> {

  public QuizResponsesTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), QuizResponse.class);
    createTableIfNotExists();
    createIndexesIfNotExists();

  }


  @PrimaryKeyPair({"player_id", "game_id", "stage", "q_num"})
  @IndexPair({"player_id", "book_name", "jadge"})
  @IndexPair({"player_id", "book_name"})
  public static record QuizResponse(String playerId, long gameId, int stage, int qNum,
      String bookName, int qid, int elapsedTime, String choice, boolean jadge,
      LocalDateTime createdAt) {
  }


}
