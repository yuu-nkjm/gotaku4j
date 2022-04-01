package org.nkjmlab.quiz.gotaku.webui;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable.Quiz;
import org.nkjmlab.quiz.gotaku.webui.QuizRecordsTable.QuizRecord;
import org.nkjmlab.quiz.gotaku.webui.QuizResponsesTable.QuizResponse;
import org.nkjmlab.util.jackson.JacksonMapper;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

public class QuizWebsocketHandler {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private static final JacksonMapper mapper = JacksonMapper.getDefaultMapper();

  private static final ExecutorService srv =
      Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 2));

  private final QuizzesTable quizzesTable;
  private final QuizResponsesTable responsesTable;
  private final QuizRecordsTable recordsTable;

  private final GotakuApplication application;

  public QuizWebsocketHandler(GotakuApplication application, DataSource dataSource,
      QuizzesTable quizzesTable) {
    this.application = application;
    this.recordsTable = new QuizRecordsTable(dataSource);
    this.responsesTable = new QuizResponsesTable(dataSource);
    this.quizzesTable = quizzesTable;
  }

  public void onClose(WsCloseContext ctx, Session session, int statusCode, String reason) {
    try {
      session.close();
      log.info("@{}({}) is closed. status code={}, reason={}", ctx.getSessionId(),
          session.hashCode(), statusCode, reason);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public void onError(Session session, Throwable cause) {
    log.error(cause.getMessage());
  }

  public void onMessage(WsMessageContext ctx) {

    RecievedJsonMessage json = mapper.toObject(ctx.message(), RecievedJsonMessage.class);

    log.debug("Call {}", json);
    switch (json.method) {
      case RELOAD_BOOKS -> {
        application.loadBooks();
        sendText(ctx.session,
            new SendJsonMessage(SendJsonMessage.MethodName.RELOAD, new Object[] {}));
      }
      case SELECT_PLAYER -> {
        ctx.attribute("player_id", json.parameters[0]);
      }
      case SELECT_BOOK -> {
        String playerId = ctx.attribute("player_id");
        String bookName = json.parameters[0].toString();
        ctx.attribute("book_name", bookName);
        sendText(ctx.session, new SendJsonMessage(SendJsonMessage.MethodName.GENRES,
            new Object[] {quizzesTable.readGenres(bookName)}));
        sendText(ctx.session,
            new SendJsonMessage(SendJsonMessage.MethodName.RANKING,
                new Object[] {recordsTable.readScoreRanking(bookName),
                    recordsTable.readAccuracyRateRanking(bookName)}));
        sendText(ctx.session, new SendJsonMessage(SendJsonMessage.MethodName.RESULTS,
            new Object[] {readResults(bookName, playerId)}));

      }
      case SELECT_GENRES -> {
        ctx.attribute("genres", json.parameters[0]);
      }
      case START_GAME -> {
        ctx.attribute("game_id", System.currentTimeMillis());
      }
      case START_STAGE -> {
        ctx.attribute("stage", json.parameters[0]);
      }
      case FINISH_STAGE -> {
      }
      case QUIZ_RESPONSE -> {
        String playerId = ctx.attribute("player_id");
        Long gameId = ctx.attribute("game_id");
        int stage = ctx.attribute("stage");
        int qNum = ctx.attribute("q_num");
        String bookName = ctx.attribute("book_name");
        int qid = ctx.attribute("qid");
        String genre = ctx.attribute("genre");
        Object[] ps = json.parameters;
        responsesTable.insert(new QuizResponse(playerId, gameId, stage, qNum, bookName, genre, qid,
            (int) ps[0], (String) ps[1], (boolean) ps[2], LocalDateTime.now()));

      }
      case NEXT_QUIZ -> {
        ctx.attribute("q_num", json.parameters[0]);
        String bookName = ctx.attribute("book_name");
        List<String> genres = ctx.attribute("genres");
        Optional<QuizJson> oQuiz = getNextQuiz(ctx, bookName, genres);

        oQuiz.ifPresent(quiz -> {
          ctx.attribute("qid", quiz.qid);
          ctx.attribute("genre", quiz.genre);
          sendText(ctx.session,
              new SendJsonMessage(SendJsonMessage.MethodName.QUIZ, new Object[] {quiz}));
        });
        if (oQuiz.isEmpty()) {
          sendText(ctx.session,
              new SendJsonMessage(SendJsonMessage.MethodName.GAME_CLEAR, new Object[] {}));
        }

      }
      case SEND_RECORD -> {
        Object[] parameters = json.parameters;
        String playerId = ctx.attribute("player_id");
        Long gameId = ctx.attribute("game_id");
        String bookName = ctx.attribute("book_name");
        int stage = ctx.attribute("stage");
        int totalQuizNumber = (int) parameters[0];
        int totalCorrectAnswers = (int) parameters[1];
        recordsTable.insert(new QuizRecord(playerId, gameId, bookName, stage, totalQuizNumber,
            totalCorrectAnswers, (int) parameters[2], LocalDateTime.now()));
      }
      default -> {
      }
    }

  }

  private Object readResults(String bookName, String playerId) {
    List<String> genres =
        quizzesTable.readGenres(bookName).stream().map(m -> m.getString("GENRE")).toList();
    return genres.stream().map(genre -> Map.of("genre", genre, "data",
        responsesTable.readPlayerLog(playerId, bookName, genre))).toList();
  }

  private Optional<QuizJson> getNextQuiz(WsMessageContext ctx, String bookName,
      List<String> genres) {
    log.info("bookName={}", bookName);
    LinkedList<Quiz> repo = ctx.attribute(bookName);
    if (repo == null) {
      repo = new LinkedList<>(quizzesTable.readBook(bookName, genres));
      ctx.attribute(bookName, repo);
    }
    if (repo.isEmpty()) {
      return Optional.empty();
    }
    int n = ThreadLocalRandom.current().nextInt(repo.size());
    Quiz q = repo.remove(n);
    log.info("Size of [{}] is [{}]", bookName, repo.size());
    return Optional.of(new QuizJson(q));
  }


  private static class QuizJson {

    public final String genre;
    public final int qid;
    public final String question;
    public final String explanation;
    public final String answer;
    public final List<String> selections;

    public QuizJson(Quiz quiz) {
      this.genre = quiz.genre();
      this.qid = quiz.qid();
      this.question = quiz.question();
      this.explanation = quiz.explanation();
      this.answer = quiz.choice1();
      List<String> s = new ArrayList<>(
          List.of(quiz.choice1(), quiz.choice2(), quiz.choice3(), quiz.choice4(), quiz.choice5()));
      Collections.shuffle(s);
      this.selections = s;
    }

    @Override
    public String toString() {
      return "QuizJson [genre=" + genre + ", qid=" + qid + ", question=" + question
          + ", explanation=" + explanation + ", answer=" + answer + ", selections=" + selections
          + "]";
    }

  }

  private static void retry(Runnable action, int maxRetry, long interval, TimeUnit timeUnit)
      throws InterruptedException {
    for (int i = 0; i < maxRetry; i++) {
      try {
        action.run();
        return;
      } catch (Exception e) {
        log.warn(e.getMessage());
      }
      timeUnit.sleep(interval);
    }
    throw new RuntimeException("Failed to try (" + maxRetry + " times).");
  }

  private static void sendText(Session session, SendJsonMessage jsonMessage) {
    srv.submit(() -> {
      RemoteEndpoint b = session.getRemote();
      synchronized (b) {
        try {
          retry(() -> {
            try {
              b.sendString(mapper.toJson(jsonMessage));
            } catch (IOException e) {
              log.warn(e.getMessage());
            }
          }, 3, 2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
      }
    });
  }



  private static class RecievedJsonMessage {

    public enum MethodName {
      FINISH_STAGE, NEXT_QUIZ, QUIZ_RESPONSE, SEND_RECORD, SELECT_PLAYER, SELECT_BOOK, START_GAME, START_STAGE, SELECT_GENRES, RELOAD_BOOKS
    }

    public MethodName method;
    public Object[] parameters;


    @SuppressWarnings("unused")
    public RecievedJsonMessage() {

    }

    @Override
    public String toString() {
      return "[" + method + ", "
          + (parameters == null ? "null"
              : Arrays.stream(parameters).map(
                  o -> o == null ? "null" : o.toString() + "(" + o.getClass().getSimpleName() + ")")
                  .toList() + "]");
    }

  }


  private static class SendJsonMessage {

    public enum MethodName {
      QUIZ, RANKING, GENRES, RESULTS, GAME_CLEAR, RELOAD
    }

    public MethodName method;
    public Object[] parameters;


    @SuppressWarnings("unused")
    public SendJsonMessage() {

    }

    public SendJsonMessage(MethodName method, Object[] parameters) {
      this.method = method;
      this.parameters = parameters;
    }


    @Override
    public String toString() {
      return "JsonMessage [method=" + method + ", parameters=" + Arrays.toString(parameters) + "]";
    }

  }

  public void onConnect(WsConnectContext ctx) {
    log.info("@{} is open", ctx.getSessionId());
  }



}
