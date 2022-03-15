package org.nkjmlab.quiz.gotaku.webui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import org.nkjmlab.quiz.gotaku.webui.WebsoketSessionsTable.WebSocketSession;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.BasicTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class WebsoketSessionsTable extends BasicTableWithDefinition<WebSocketSession> {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();



  private Map<Integer, Session> sessions = new ConcurrentHashMap<>();


  public WebsoketSessionsTable(Sorm sorm) {
    super(sorm, WebSocketSession.class, TableDefinition.builder(WebSocketSession.class).build());
    dropTableIfExists();
    createIndexesIfNotExists();
  }

  public record WebSocketSession(int sessionId, String gameId, String userId) {
  }


  void registerSession(String gameId, String userId, Session session) {
    getOrm().acceptHandler(conn -> {
      int sessionId = session.hashCode();
      WebsoketSessionsTable.WebSocketSession ws = new WebSocketSession(sessionId, gameId, userId);
      if (conn.exists(ws)) {
        log.warn("{} already exists.", ws);
        return;
      }
      conn.insert(ws);
      sessions.put(sessionId, session);
      log.info("WebSocket is registered={}", ws);
    });
  }

  void updateSession(int sessionId, String gameId, String userId) {
    update(new WebSocketSession(sessionId, gameId, userId));
  }

  Optional<String> removeSession(Session session) {
    for (Entry<Integer, Session> e : sessions.entrySet()) {
      if (!e.getValue().equals(session)) {
        continue;
      }
      sessions.remove(e.getKey());
      Optional<String> ret = getOrm().applyHandler(conn -> {
        WebsoketSessionsTable.WebSocketSession gs =
            conn.selectByPrimaryKey(WebsoketSessionsTable.WebSocketSession.class, e.getKey());
        conn.delete(gs);
        return Optional.of(gs.gameId());
      });
      return ret;
    }
    return Optional.empty();
  }



}
