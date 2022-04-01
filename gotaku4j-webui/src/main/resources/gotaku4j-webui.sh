cd `dirname $0`
cd ../
java -cp classes:lib/* -Dfile.encoding=UTF-8 org.nkjmlab.quiz.gotaku.webui.GotakuApplication $@
