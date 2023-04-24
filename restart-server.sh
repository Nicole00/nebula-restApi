 ps -wf | grep "nebula-restApi-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}'|xargs kill -9
 nohup java -jar nebula-restApi-1.0-SNAPSHOT.jar &
