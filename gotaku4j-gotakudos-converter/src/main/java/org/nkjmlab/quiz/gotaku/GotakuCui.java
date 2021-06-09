package org.nkjmlab.quiz.gotaku;

import java.util.Scanner;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuFileConverter;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuiz;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuizBook;
import org.nkjmlab.quiz.gotaku.gotakudos.QuizResource;
import org.nkjmlab.quiz.gotaku.util.ResourceUtils;

public class GotakuCui {

  public static Scanner sc = new Scanner(System.in);

  public static void main(String[] args) {
    GotakuCui gotakuCui = new GotakuCui();
    GotakuQuizBook gotakuQuizBook =
        new GotakuFileConverter().parse(ResourceUtils.getFile("/quizbooks/5tq/正統派クイズ2000題"));
    gotakuCui.startGame(gotakuQuizBook);

  }

  private void startGame(GotakuQuizBook gotakuQuizBook) {
    QuizResource quizResource = new QuizResource(gotakuQuizBook);
    int border = 40;
    int totalScore = 0;
    gotakuQuizBook.getToc().forEach(s -> System.out.println(s));
    System.out.println();

    for (int i = 0; i < 8; i++) {
      System.out.println("【ステージ " + (i + 1) + "開始】 ");
      System.out.println();

      int stageScore = startStage(quizResource);
      totalScore += stageScore;

      if (stageScore >= border) {
        System.out.println("ステージ " + (i + 1) + " クリア！次のステージに進みます");
      } else {
        System.out.println("ステージ失敗… ");
        break;
      }
      if (i == 7) {
        System.out.println("最終ステージまでクリアしました！");
      }
    }
    System.err.println("最終スコアは" + totalScore + "点でした");

  }



  private int startStage(QuizResource quizResource) {

    int score = 0;
    int i = 0;

    while (true) {
      GotakuQuiz quiz = quizResource.getNextQuiz();

      System.out.println("問題" + (i + 1) + ". " + quiz.getQuestion());
      String msg = "";
      for (int j = 0; j < quiz.getSelections().size(); j++) {
        System.out.println((j + 1) + "." + quiz.getSelections().get(j));
      }
      System.out.println(msg);

      int input = getUserInputBetween(1, 5) - 1;
      String ans = quiz.getAnswer();
      String ansMsg = "(正解は「" + ans + "」)";
      if (ans.equals(quiz.getSelections().get(input))) {
        System.err.println("正解! " + ansMsg);
        score++;
      } else {
        System.out.println("ざんねん... " + ansMsg);
      }
      i++;
      System.out.println("--------------");
      if (i == 10) {
        break;
      }
    }
    System.err.println((score * 10) + "点 / " + (i * 10) + "点でした");
    return score * 10;
  }

  private static int getUserInputBetween(int fromInclusive, int toInclusive) {
    while (true) {
      System.out.print("(input " + fromInclusive + "-" + toInclusive + ") > ");
      int input = sc.nextInt();
      if (fromInclusive <= input && input <= toInclusive) {
        return input;
      }
    }
  }

}
