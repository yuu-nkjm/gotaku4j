package org.nkjmlab.quiz.gotaku.webui;

import java.io.File;
import javax.sql.DataSource;
import org.nkjmlab.quiz.gotaku.webui.util.TemplateEngineBuilder;
import org.nkjmlab.quiz.gotaku.webui.util.ViewModel;
import org.nkjmlab.util.h2.LocalDataSourceFactory;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.json.FileDatabaseConfigJson;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

public class GotakuApplication {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(GotakuApplication.class);

  public static final File APP_ROOT_DIR = ResourceUtils.getFile("/");
  private static final String WEB_ROOT_DIR_NAME = "/webroot";
  private static final File WEB_ROOT_DIR = new File(APP_ROOT_DIR, WEB_ROOT_DIR_NAME);
  public static final File PROBLEM_ROOT_DIR = new File(APP_ROOT_DIR, "problems");

  private static long THYMELEAF_EXPIRE_TIME_MILLI_SECOND = 1 * 1000;
  private final Javalin app;
  private final DataSource dataSourceForFileDb;

  public static void main(String[] args) {
    int port = args.length == 0 ? 7890 : Integer.valueOf(args[0]);
    ProcessUtils.stopProcessBindingPortIfExists(port);
    new GotakuApplication().start(port);
  }

  public GotakuApplication() {
    FileDatabaseConfigJson conf = JacksonMapper.getDefaultMapper()
        .toObject(ResourceUtils.getFile("/h2.conf"), FileDatabaseConfigJson.Builder.class).build();
    LocalDataSourceFactory factory = LocalDataSourceFactory.builder(conf).build();
    log.info("{}", conf);
    log.info("factory=[{}]", factory);
    this.dataSourceForFileDb = factory.createMixedModeDataSource();
    this.app = createJavalin();

  }

  private Javalin createJavalin() {
    RecordsTable recordsTable = new RecordsTable(dataSourceForFileDb);
    JavalinThymeleaf.configure(new TemplateEngineBuilder().setPrefix("/templates/")
        .setTtlMs(THYMELEAF_EXPIRE_TIME_MILLI_SECOND).build());

    Javalin app = Javalin.create(config -> {
      config.addStaticFiles(WEB_ROOT_DIR_NAME, Location.CLASSPATH);
      config.autogenerateEtags = true;
      config.enableCorsForAllOrigins();
    });

    app.ws("/websocket/play", ws -> {
      // ws.onConnect(ctx -> QuizWebsocketHandler.getHandler().onConnect(ctx.session));
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
      ctx.render(pageName, createDefaultModel());
    });

    return app;
  }

  public void start(int port) {
    app.start(port);
  }

  private static ViewModel createDefaultModel() {
    ViewModel model =
        new ViewModel.Builder().setFileModifiedDate(WEB_ROOT_DIR, true, "js", "css").build();
    return model;
  }


}
