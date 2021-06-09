package org.nkjmlab.quiz.gotaku.gotakudos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class QuizResource {

  private final GotakuQuizBook gotakuQuizBook;
  private final LinkedList<GotakuQuiz> quizeQueue;

  public QuizResource(GotakuQuizBook gotakuQuizBook) {
    this.gotakuQuizBook = gotakuQuizBook;
    List<GotakuQuiz> t =
        gotakuQuizBook.allQuizzes().stream().map(q -> q.toShuffled()).collect(Collectors.toList());
    Collections.shuffle(t);
    this.quizeQueue = new LinkedList<>(t);
  }

  public GotakuQuiz getNextQuiz() {
    return quizeQueue.poll();
  }

}
