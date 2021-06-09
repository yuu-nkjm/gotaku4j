package org.nkjmlab.quiz.gotaku.webui;

import static org.nkjmlab.quiz.gotaku.util.JacksonUtils.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuFileConverter;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuiz;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuizBook;
import org.nkjmlab.quiz.gotaku.gotakudos.QuizResource;
import org.nkjmlab.quiz.gotaku.util.JacksonUtils;
import org.nkjmlab.quiz.gotaku.util.ResourceUtils;
import org.nkjmlab.quiz.gotaku.util.Try;

public class QuizWebsocketHandler {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(QuizWebsocketHandler.class);

  private static final ExecutorService srv =
      Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 2));


  private static final Map<String, GotakuQuizBook> gotakuQuizBooks = new GotakuFileConverter()
      .parseAll(Try.getOrThrow(() -> ResourceUtils.getFile("/quizbooks/5tq/"), Try::rethrow));

  private static final Map<String, QuizWebsocketHandler> instances = new ConcurrentHashMap<>();

  private QuizResource quizResource;

  public static QuizWebsocketHandler getHandler(String sessionId) {
    return instances.computeIfAbsent(sessionId, sid -> new QuizWebsocketHandler());
  }



  public void onMessage(Session session, String text, RecordsTable recordsTable) {
    JsonMessage json = JacksonUtils.readValue(text, JsonMessage.class);

    log.debug("{}", json);
    switch (json.method) {
      case JsonMessage.START_GAME:
        sendBookTitles(session,
            gotakuQuizBooks.keySet().stream().sorted().collect(Collectors.toList()));
        return;
      case JsonMessage.START_BOOK:
        this.quizResource = new QuizResource(gotakuQuizBooks.get(json.parameters[0]));
        // logger.debug("[{}] is set", quizResource.getBookName());
        return;
      case JsonMessage.START_STAGE:
        return;
      case JsonMessage.FINISH_STAGE:
        return;
      case JsonMessage.NEXT_QUIZ:
        GotakuQuiz gotakuQuiz = quizResource.getNextQuiz();
        sendQuiz(session, gotakuQuiz);
        return;
      case JsonMessage.QUIZ_RESULT:
        return;
      case JsonMessage.SEND_RECORD: {
        Object[] parameters = json.parameters;
        recordsTable.insert((String) parameters[0], (int) parameters[1], (int) parameters[2],
            (int) parameters[3]);
        return;
      }
      default:
        return;
    }

  }

  public void onConnect(Session session) {}

  public void onClose(Session session, int statusCode, String reason) {
    try {
      session.close();
      log.info("@{} is closed. status code={}, reason={}", session.hashCode(), statusCode, reason);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public void onError(Session session, Throwable cause) {
    log.error(cause.getMessage());
  }

  private void sendBookTitles(Session session, List<String> titles) {
    sendText(session, writeValue(new JsonMessage(JsonMessage.BOOK_TITLES, new Object[] {titles})));
  }


  private static void sendQuiz(Session session, GotakuQuiz gotakuQuiz) {
    sendText(session, writeValue(new JsonMessage(JsonMessage.QUIZ, new Object[] {gotakuQuiz})));
  }

  private static void sendText(Session session, String text) {
    srv.submit(() -> {
      RemoteEndpoint b = session.getRemote();
      synchronized (b) {
        try {
          retry(() -> {
            try {
              b.sendString(text);
            } catch (IOException e) {
              log.warn(e.getMessage());
            }
          }, 3, 2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
      }
    });
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


  private static class JsonMessage {
    public static final String QUIZ = "QUIZ";
    public static final String BOOK_TITLES = "BOOK_TITLES";
    public static final String START_GAME = "startGame";
    public static final String START_BOOK = "startBook";
    public static final String START_STAGE = "startStage";
    public static final String FINISH_STAGE = "finishStage";
    public static final String NEXT_QUIZ = "nextQuiz";
    public static final String QUIZ_RESULT = "quizResult";
    public static final String SEND_RECORD = "sendRecord";

    public String method;
    public Object[] parameters;

    public JsonMessage() {}

    public JsonMessage(String method, Object[] parameters) {
      this.method = method;
      this.parameters = parameters;
    }


    @Override
    public String toString() {
      return "JsonMessage [method=" + method + ", parameters=" + Arrays.toString(parameters) + "]";
    }

  }



}
