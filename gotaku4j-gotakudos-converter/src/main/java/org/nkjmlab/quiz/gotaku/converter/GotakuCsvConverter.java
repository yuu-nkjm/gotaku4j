package org.nkjmlab.quiz.gotaku.converter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable.Quiz;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKey;

public class GotakuCsvConverter {

  private final QuizzesTable quizTable;

  public GotakuCsvConverter(QuizzesTable quizTable) {
    this.quizTable = quizTable;

  }

  @OrmRecord
  public static record QuizCsvRow(@PrimaryKey int qid, String question, String explanation,
      String choice1, String choice2, String choice3, String choice4, String choice5) {
  }


  public static class QuizCsvRowsTable extends BasicH2Table<QuizCsvRow> {

    public QuizCsvRowsTable(DataSource dataSorce) {
      super(Sorm.create(dataSorce), QuizCsvRow.class);
      createTableIfNotExists();
    }

  }


  public void parseAll(File _5tqsRootDir) {
    Arrays.stream(_5tqsRootDir.listFiles()).filter(subDir -> subDir.isDirectory())
        .forEach(subDir -> parse(subDir));
  }

  private void parse(File _5tqDir) {
    String bookName = _5tqDir.getName();

    List<File> csvFiles = Arrays.stream(_5tqDir.listFiles())
        .filter(fileInSubDir -> fileInSubDir.getName().toLowerCase().endsWith(".csv")).toList();


    csvFiles.stream().forEach(csvFile -> toQuizGenre(bookName, csvFile));


  }

  public void toQuizGenre(String bookName, File csvFile) {
    DataSource dataSorce = Sorm.createDataSource("jdbc:h2:mem:gotaku;DB_CLOSE_DELAY=-1", "sa", "");
    QuizCsvRowsTable table = new QuizCsvRowsTable(dataSorce);
    List<QuizCsvRow> rows = table.readCsvWithHeader(csvFile);
    table.dropTableIfExists();

    String genre = csvFile.getName().substring(0, csvFile.getName().length() - 4);
    quizTable.merge(rows.stream().map(row -> toQuiz(bookName, genre, row)).toList());

  }

  private Quiz toQuiz(String bookName, String genre, QuizCsvRow row) {
    return new Quiz(bookName, genre, row.qid, convertImageUrlToImageTag(row.question), row.choice1,
        row.choice2, row.choice3, row.choice4, row.choice5,
        convertImageUrlToImageTag(row.explanation));
  }


  private static final String IMG_SRC_REGEX =
      "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

  static String convertImageUrlToImageTag(String text) {
    if (text == null) {
      return null;
    }
    return text.replaceAll("\\[\\?\\]", "<span class='badge bg-secondary'>?</span>")
        .replaceAll("\\[r (.*?)\\]", "<span class='text-color text-danger'>$1</span>")
        .replaceAll("\\[b (.*?)\\]", "<span class='text-color text-primary'>$1</span>")
        .replaceAll("\\[y (.*?)\\]", "<span class='text-color text-warning'>$1</span>")
        .replaceAll("\\[g (.*?)\\]", "<span class='text-color text-success'>$1</span>")
        .replaceAll("\\[i (.*?)\\]", "<img class='img-thumbnail' style='max-width:400px' src='$1'>")
        .replaceAll("\\[(.*?)(.jpg|.png)\\]", "<img class='img-thumbnail' style='max-width:400px' src='$1$2'>")
        .replaceAll("\\[(.*?) (http.*?)\\]", "<a href='$2' target='_blank'>$1</a>");

  }



}
