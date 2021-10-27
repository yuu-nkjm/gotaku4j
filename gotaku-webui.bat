cd /d %~dp0
java -cp gotaku4j-webui/target/classes;gotaku4j-webui/target/dependency/*;gotaku4j-gotakudos-converter/target/classes;gotaku4j-gotakudos-converter/target/dependency/*;lib/* -Dfile.encoding=UTF-8 org.nkjmlab.quiz.gotaku.webui.GotakuApplication

