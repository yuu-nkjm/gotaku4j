package org.nkjmlab.quiz.gotaku.webui;

import java.io.File;
import javax.sql.DataSource;
import org.nkjmlab.util.h2.H2LocalDataSourceFactory;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.json.FileDatabaseConfigJson;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import org.nkjmlab.util.javax.servlet.ViewModel;
import org.nkjmlab.util.thymeleaf.TemplateEngineBuilder;
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
  private final DataSource dataSourceForFileDb;

  public static void main(String[] args) {
    int port = args.length == 0 ? 7890 : Integer.valueOf(args[0]);
    ProcessUtils.stopProcessBindingPortIfExists(port);
    new GotakuApplication().start(port);
  }

  public GotakuApplication() {
    FileDatabaseConfigJson conf = JacksonMapper.getDefaultMapper()
        .toObject(ResourceUtils.getResourceAsFile("/h2.conf"), FileDatabaseConfigJson.Builder.class)
        .build();
    H2LocalDataSourceFactory factory = H2LocalDataSourceFactory.builder(conf).build();
    log.info("{}, factory=[{}]", conf, factory);
    this.dataSourceForFileDb = factory.createMixedModeDataSource();
    this.app = createJavalin();

  }

  private Javalin createJavalin() {
    QuizRecordsTable recordsTable = new QuizRecordsTable(dataSourceForFileDb);

    JavalinThymeleaf.configure(new TemplateEngineBuilder().setPrefix("/templates/")
        .setTtlMs(THYMELEAF_EXPIRE_TIME_MILLI_SECOND).build());

    Javalin app = Javalin.create(config -> {
      config.addStaticFiles(WEB_ROOT_DIR_NAME, Location.CLASSPATH);
      config.autogenerateEtags = true;
      config.enableCorsForAllOrigins();
    });

    app.ws("/websocket/play", ws -> {
      ws.onClose(ctx -> QuizWebsocketHandler.getHandler(ctx.getSessionId()).onClose(ctx.session,
          ctx.status(), ctx.reason()));
      ws.onError(ctx -> QuizWebsocketHandler.getHandler(ctx.getSessionId()).onError(ctx.session,
          ctx.error()));
      ws.onMessage(ctx -> QuizWebsocketHandler.getHandler(ctx.getSessionId()).onMessage(ctx.session,
          ctx.message(), recordsTable));
    });

    app.get("/app", ctx -> {
      ctx.redirect("/app/index.html");
    });

    app.get("/app/{pageName}", ctx -> {
      String pageName =
          ctx.pathParam("pageName") == null ? "index.html" : ctx.pathParam("pageName");
      ctx.render(pageName, createDefaultModel().getMap());
    });

    return app;
  }

  public void start(int port) {
    app.start(port);
  }

  private static ViewModel createDefaultModel() {
    ViewModel model = ViewModel.builder().setFileModifiedDate(WEB_ROOT_DIR, 10, "js", "css").build();
    return model;
  }


}
