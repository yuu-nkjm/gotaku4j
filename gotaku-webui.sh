cd `dirname $0`
cd ../
java -cp gotaku4j-webui/target/classes:gotaku4j-gotakudos-converter/target/classes:lib/* -Dfile.encoding=UTF-8 org.nkjmlab.quiz.gotaku.webui.GotakuApplication $@
