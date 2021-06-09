package org.nkjmlab.quiz.gotaku.util;

public class FileDbConfig {

  private String dbDir;
  private String dbName;
  private String username;
  private String password;

  public FileDbConfig() {

  }

  public String toJdbcUrl() {
    return "jdbc:h2:file:" + dbDir + "/" + dbName;
  }

  public String getDbDir() {
    return dbDir;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {

    this.dbName = dbName;
  }

  public void setDbDir(String dbDir) {
    this.dbDir = dbDir;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "FileDbConfig [dbDir=" + dbDir + ", dbName=" + dbName + ", username=" + username
        + ", password=" + password + "]";
  }


}
