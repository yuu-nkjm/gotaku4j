package org.nkjmlab.quiz.gotaku.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuiz;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuizBook;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable.Quiz;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.result.RowMap;
import org.nkjmlab.sorm4j.sql.OrderedParameterSqlParser;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.IndexColumns;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyColumns;

public class QuizzesTable extends BasicH2Table<Quiz> {

  @OrmRecord
  @PrimaryKeyColumns({"book_name", "genre", "qid"})
  @IndexColumns({"book_name", "genre", "qid"})
  public static record Quiz(String bookName, String genre, int qid, String question, String choice1,
      String choice2, String choice3, String choice4, String choice5, String explanation) {

  }

  public QuizzesTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), Quiz.class);
    dropTableIfExists();
    createTableIfNotExists();
    createIndexesIfNotExists();
  }

  public List<String> readGenreNames(String bookName) {
    return getOrm().readList(String.class,
        "select distinct genre from " + getTableName() + " where book_name=?", bookName);
  }


  public List<RowMap> readGenres(String bookName) {
    return getOrm().readList(RowMap.class, "select genre, count(*) as num from " + getTableName()
        + " where book_name=? group by genre order by genre", bookName);
  }

  public List<String> getBookNames() {
    return getOrm().readList(String.class, "select distinct book_name from " + getTableName());
  }


  public void mergeBook(GotakuQuizBook book) {
    AtomicInteger id = new AtomicInteger();
    book.getGenres()
        .forEach(section -> merge(section.getQuizzes().stream().map(
            q -> toQuizRecord(id.getAndIncrement(), book.getBookName(), section.getGenreName(), q))
            .collect(Collectors.toList())));

  }

  private static Quiz toQuizRecord(int id, String bookName, String genreName, GotakuQuiz q) {
    String question = q.getQuestion();
    List<String> ss = q.getSelections();
    String choice1 = ss.get(0);
    String choice2 = ss.get(1);
    String choice3 = ss.get(2);
    String choice4 = ss.get(3);
    String choice5 = ss.get(4);
    return new Quiz(bookName, genreName, id, question, choice1, choice2, choice3, choice4, choice5,
        "");
  }


  public List<Quiz> readBook(String bookName, List<String> genres) {
    return readList(OrderedParameterSqlParser.parse(
        "select * from " + getTableName() + " where book_name=? and genre in(<?>)", bookName,
        genres));
  }



}
