package org.nkjmlab.quiz.gotaku.gotakudos;

import static org.nkjmlab.sorm4j.sql.schema.TableSchema.Keyword.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.sql.schema.TableSchema;
import org.nkjmlab.sorm4j.sql.schema.TableSchema.Keyword;

public class QuizTable {

  private final TableSchema schema;
  private final Sorm sorm;

  public static final String TABLE_NAME = "QUIZ";

  private static final String ID = "id";
  private static final String BOOK_NAME = "book_name";
  private static final String GENRE = "genre";
  private static final String QUESTION = "question";
  private static final String S1 = "s1";
  private static final String S2 = "s2";
  private static final String S3 = "s3";
  private static final String S4 = "s4";
  private static final String S5 = "s5";



  public QuizTable(DataSource dataSorce) {
    this.sorm = Sorm.create(dataSorce);
    this.schema = TableSchema.builder().setTableName(TABLE_NAME)
        .addColumnDefinition(ID, Keyword.INT, Keyword.AUTO_INCREMENT, Keyword.PRIMARY_KEY)
        .addColumnDefinition(BOOK_NAME, VARCHAR).addColumnDefinition(BOOK_NAME, VARCHAR)
        .addColumnDefinition(GENRE, VARCHAR).addColumnDefinition(QUESTION, VARCHAR)
        .addColumnDefinition(S1, VARCHAR).addColumnDefinition(S2, VARCHAR)
        .addColumnDefinition(S3, VARCHAR).addColumnDefinition(S4, VARCHAR)
        .addColumnDefinition(S5, VARCHAR).build();
  }

  public void dropTableIfExists() {
    sorm.accept(client -> client.executeUpdate(schema.getDropTableIfExistsStatement()));
  }

  public void createTableAndIndexesIfNotExists() {
    sorm.accept(client -> {
      client.executeUpdate(schema.getCreateTableIfNotExistsStatement());
      schema.getCreateIndexIfNotExistsStatements()
          .forEach(createIndexStatement -> client.executeUpdate(createIndexStatement));
    });
  }

  public void insert(GotakuQuizBook book) {
    book.getGenres()
        .forEach(section -> sorm.accept(conn -> conn.insert(section.getQuizzes().stream()
            .map(q -> new Quiz(book.getBookName(), section.getGenreName(), q))
            .collect(Collectors.toList()))));
  }

  public List<Quiz> readAllQuizzes() {
    return sorm.apply(conn -> conn.readList(Quiz.class, "select * from " + TABLE_NAME));
  }

  public List<String> readAllGenreNames() {
    return sorm.apply(
        conn -> conn.readList(String.class, "select distinct " + GENRE + " from " + TABLE_NAME));
  }


  public static class Quiz {

    public int id;
    public String bookName;
    public String genre;
    public String question;
    public String s1;
    public String s2;
    public String s3;
    public String s4;
    public String s5;

    // for ORM
    public Quiz() {}

    public Quiz(String bookName, String genre, org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuiz q) {
      this.bookName = bookName;
      this.genre = genre;
      this.question = q.getQuestion();
      List<String> ss = q.getSelections();
      s1 = ss.get(0);
      s2 = ss.get(1);
      s3 = ss.get(2);
      s4 = ss.get(3);
      s5 = ss.get(4);
    }
  }


}
