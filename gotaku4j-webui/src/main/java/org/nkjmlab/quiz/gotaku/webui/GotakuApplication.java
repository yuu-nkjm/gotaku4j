package org.nkjmlab.quiz.gotaku.webui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.converter.GotakuCsvConverter;
import org.nkjmlab.quiz.gotaku.converter.GotakuFileConverter;
import org.nkjmlab.quiz.gotaku.gotakudos.GotakuQuizBook;
import org.nkjmlab.quiz.gotaku.model.QuizzesTable;
import org.nkjmlab.sorm4j.util.h2.datasource.H2LocalDataSourceFactory;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProcess;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProperties;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.function.Try;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import org.nkjmlab.util.java.web.ViewModel;
import org.nkjmlab.util.java.web.ViewModel.Builder;
import org.nkjmlab.util.thymeleaf.ThymeleafTemplateEnginBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

public class GotakuApplication {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public static final File APP_ROOT_DIR = ResourceUtils.getResourceAsFile("/");
  private static final String WEB_ROOT_DIR_NAME = "/webroot";
  private static final File WEB_ROOT_DIR = new File(APP_ROOT_DIR, WEB_ROOT_DIR_NAME);
  public static final File PROBLEM_ROOT_DIR = new File(APP_ROOT_DIR, "problems");

  private static final long THYMELEAF_EXPIRE_TIME_MILLI_SECOND = 1 * 1000;
  private final Javalin app;
  private final DataSource dataSource;
  private final QuizzesTable quizzesTable;


  public static void main(String[] args) {
    int port = args.length == 0 ? 7890 : Integer.valueOf(args[0]);
    ProcessUtils.stopProcessBindingPortIfExists(port);
    new H2TcpServerProcess(H2TcpServerProperties.builder().build()).awaitStart();
    new GotakuApplication().start(port);
  }

  public GotakuApplication() {
    H2LocalDataSourceFactory factory =
        JacksonMapper.getDefaultMapper().toObject(ResourceUtils.getResourceAsFile("/h2.conf"),
            H2LocalDataSourceFactory.Builder.class).build();

    log.info("factory=[{}]", factory);
    this.dataSource = factory.createServerModeDataSource();
    this.quizzesTable = new QuizzesTable(dataSource);
    this.app = createJavalin();
  }

  private Javalin createJavalin() {

    JavalinThymeleaf.configure(ThymeleafTemplateEnginBuilder.builder()
        .setTtlMs(THYMELEAF_EXPIRE_TIME_MILLI_SECOND).build());

    Javalin app = Javalin.create(config -> {
      config.addStaticFiles(WEB_ROOT_DIR_NAME, Location.CLASSPATH);
      config.autogenerateEtags = true;
      config.enableCorsForAllOrigins();
    });


    Map<String, GotakuQuizBook> gotakuQuizBooks = new GotakuFileConverter().parseAll(
        Try.getOrElseThrow(() -> ResourceUtils.getResourceAsFile("/quizbooks/5tq/"), Try::rethrow));
    gotakuQuizBooks.values().forEach(b -> quizzesTable.mergeBook(b));
    loadBooks();

    QuizWebsocketHandler handler = new QuizWebsocketHandler(this, dataSource, quizzesTable);

    app.ws("/websocket/play", ws -> {
      ws.onConnect(ctx -> handler.onConnect(ctx));
      ws.onClose(ctx -> handler.onClose(ctx, ctx.session, ctx.status(), ctx.reason()));
      ws.onError(ctx -> handler.onError(ctx.session, ctx.error()));
      ws.onMessage(ctx -> handler.onMessage(ctx));
    });

    app.get("/app", ctx -> {
      ctx.redirect("/app/index.html");
    });

    app.get("/app/{pageName}", ctx -> {
      String pageName =
          ctx.pathParam("pageName") == null ? "index.html" : ctx.pathParam("pageName");

      Builder model = createDefaultModel();
      try {
        List<String> players = Arrays
            .asList(Files.readAllLines(ResourceUtils.getResourceAsFile("/players.csv").toPath())
                .get(0).split(","));
        model.put("players", players);
      } catch (IOException e) {
        Try.rethrow(e);
      }


      model.put("books", quizzesTable.getBookNames());
      ctx.render(pageName, model.build());
    });

    return app;
  }

  public void start(int port) {
    app.start(port);
  }

  private static Builder createDefaultModel() {
    return ViewModel.builder().setFileModifiedDate(WEB_ROOT_DIR, 10, "js", "css");
  }

  public void loadBooks() {
    File _5tqsDir = ResourceUtils.getResourceAsFile("/quizbooks/5tqcsv");
    new GotakuCsvConverter(quizzesTable).parseAll(_5tqsDir);
  }


}
