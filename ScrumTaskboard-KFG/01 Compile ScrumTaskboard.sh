export JAVA_HOME=/Library/Java/Home
echo $JAVA_HOME

java -jar Coco.jar ScrumTaskboard.atg -frames frm -o src
javac -d cls src/Parser.java src/Scanner.java src/ScrumTaskboard.java 
jar cfmv ScrumTaskboard.jar ScrumTaskboard.mf -C cls .
