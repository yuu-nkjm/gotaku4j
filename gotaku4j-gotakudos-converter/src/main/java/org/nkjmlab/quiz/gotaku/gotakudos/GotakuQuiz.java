package org.nkjmlab.quiz.gotaku.gotakudos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GotakuQuiz {

  private final String question;
  private final String answer;
  private final List<String> selections;

  public GotakuQuiz(String question, String answer, String s1, String s2, String s3, String s4,
      String s5) {
    this.question = question;
    this.answer = answer;
    this.selections = Collections.unmodifiableList(List.of(s1, s2, s3, s4, s5));
  }


  public GotakuQuiz(String question, String answer, List<String> selections) {
    this(question, answer, selections.get(0), selections.get(1), selections.get(2),
        selections.get(3), selections.get(4));
  }


  public GotakuQuiz toShuffled() {
    List<String> s = new ArrayList<>(selections);
    Collections.shuffle(s);
    return new GotakuQuiz(question, answer, s);
  }

  public String getQuestion() {
    return question;
  }

  public String getAnswer() {
    return answer;
  }

  public List<String> getSelections() {
    return selections;
  }

  @Override
  public String toString() {
    return "GotakuQuiz [question=" + question + ", answer=" + answer + ", selections=" + selections
        + "]";
  }

}
