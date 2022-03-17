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
import org.nkjmlab.sorm4j.util.h2.BasicH2TableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.annotation.IndexPair;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyPair;

public class QuizzesTable extends BasicH2TableWithDefinition<Quiz> {

  @OrmRecord
  @PrimaryKeyPair({"book_name", "qid"})
  @IndexPair({"book_name", "qid"})
  public static record Quiz(String bookName, String genre, int qid, String question, String choice1,
      String choice2, String choice3, String choice4, String choice5, String explanation) {

  }

  public QuizzesTable(DataSource dataSorce) {
    super(Sorm.create(dataSorce), Quiz.class);
    createTableIfNotExists();
    createIndexesIfNotExists();
  }


  public List<String> readGenreNames() {
    return getOrm().readList(String.class, "select distinct genre from " + getTableName());
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


  public List<Quiz> readBook(String bookName) {
    return selectListAllEqual("book_name", bookName);
  }



}
