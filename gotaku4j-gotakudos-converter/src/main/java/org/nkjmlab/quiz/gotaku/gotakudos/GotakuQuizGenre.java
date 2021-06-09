package org.nkjmlab.quiz.gotaku.gotakudos;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GotakuQuizGenre {
  private final String genreName;
  private final List<GotakuQuiz> quizzes;

  public GotakuQuizGenre(String genre, List<GotakuQuiz> gotakuQuizzes) {
    this.genreName = genre;
    this.quizzes = gotakuQuizzes;
  }

  @Override
  public String toString() {
    return "GotakuQuizGenre [genreName=" + genreName + ", quizzes=" + quizzes + "]";
  }

  public List<GotakuQuiz> getQuizzes() {
    return quizzes;
  }

  public String getGenreName() {
    return genreName;
  }

  public GotakuQuiz getQuiz(int index) {
    return getQuizzes().get(index);
  }

  public GotakuQuiz getRandomQuiz() {
    return getQuiz(ThreadLocalRandom.current().nextInt(quizzes.size()));
  }

}
