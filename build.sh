 mvn clean package -Dmaven.test.skip=true
 cp target/nebula-restApi-1.0-SNAPSHOT.jar .
 nohup java -jar nebula-restApi-1.0-SNAPSHOT.jar &
