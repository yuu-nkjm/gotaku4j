package org.nkjmlab.quiz.gotaku.webui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.table.TableSchema;
import org.nkjmlab.sorm4j.table.TableSchema.Keyword;

public class WebsoketSessionsTable {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(WebsoketSessionsTable.class);


  public static final String TABLE_NAME = "WEBSOCKET_SESSIONS";

  private static final String SESSION_ID = "session_id";
  private static final String USER_ID = "user_id";
  private static final String GAME_ID = "game_id";
  private static final String CREATED_AT = "created_at";

  private Map<Integer, Session> sessions = new ConcurrentHashMap<>();

  private TableSchema schema;
  private Sorm sorm;

  public WebsoketSessionsTable(Sorm client) {
    this.sorm = client;
    this.schema = TableSchema.builder().setTableName(TABLE_NAME)
        .addColumnDefinition(SESSION_ID, Keyword.INT, Keyword.PRIMARY_KEY)
        .addColumnDefinition(USER_ID, Keyword.VARCHAR).addColumnDefinition(GAME_ID, Keyword.VARCHAR)
        .addColumnDefinition(CREATED_AT, Keyword.TIMESTAMP).addIndexDefinition(GAME_ID).build();
  }


  public void dropAndCreate() {
    sorm.accept(client -> {
      client.executeUpdate(schema.getDropTableIfExistsStatement());
      client.executeUpdate(schema.getCreateTableIfNotExistsStatement());
    });
  }


  void registerSession(String gameId, String userId, Session session) {
    sorm.accept(client -> {
      int sessionId = session.hashCode();
      WebsoketSessionsTable.WebSocketSession ws = new WebSocketSession(sessionId, gameId, userId);
      if (client.exists(ws)) {
        log.warn("{} already exists.", ws);
        return;
      }
      client.insert(ws);
      sessions.put(sessionId, session);
      log.info("WebSocket is registered={}", ws);
    });
  }

  void updateSession(int sessionId, String gameId, String userId) {
    sorm.accept(client -> client.update(new WebSocketSession(sessionId, gameId, userId)));
  }

  Optional<String> removeSession(Session session) {
    for (Entry<Integer, Session> e : sessions.entrySet()) {
      if (e.getValue().equals(session)) {
        sessions.remove(e.getKey());
        return sorm.apply(client -> {
          WebsoketSessionsTable.WebSocketSession gs =
              client.readByPrimaryKey(WebsoketSessionsTable.WebSocketSession.class, e.getKey());
          client.delete(gs);
          return Optional.of(gs.getGameId());
        });
      }
    }
    return Optional.empty();
  }

  public static class WebSocketSession {

    private int sessionId;
    private String gameId;
    private String userId;

    public WebSocketSession() {}

    public WebSocketSession(int sessionId, String gameId, String userId) {
      this.sessionId = sessionId;
      this.gameId = gameId;
      this.userId = userId;
    }

    public int getSessionId() {
      return sessionId;
    }

    public String getGameId() {
      return gameId;
    }

    public String getUserId() {
      return userId;
    }


  }

}
