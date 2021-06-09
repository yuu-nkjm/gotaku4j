package org.nkjmlab.quiz.gotaku.gotakudos;

import java.util.List;
import java.util.stream.Collectors;

public class GotakuQuizBook {
  private final String bookName;
  private final List<String> toc;
  private final List<GotakuQuizGenre> genres;

  public GotakuQuizBook(String bookName, List<String> toc, List<GotakuQuizGenre> genres) {
    this.bookName = bookName;
    this.toc = toc;
    this.genres = genres;
  }

  public List<GotakuQuizGenre> getGenres() {
    return genres;
  }

  public GotakuQuizGenre getGenre(int index) {
    return genres.get(index);
  }

  public List<String> getToc() {
    return toc;
  }

  public String getBookName() {
    return bookName;
  }

  @Override
  public String toString() {
    return "GotakuQuizBook [bookName=" + bookName + ", toc=" + toc + ", genres=" + genres + "]";
  }

  public List<GotakuQuiz> allQuizzes() {
    return getGenres().stream().flatMap(g -> g.getQuizzes().stream()).collect(Collectors.toList());
  }



}
