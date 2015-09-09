Installations Beschreibung

1. Project in java/projects kopieren

2. folgende Zeilen in den mandant.classpath eintragen

java/projects../../sftpclient/classes
java/projects/sftpclient/classes
java/projects/sftpclient/includejar/jsch-0.1.53.jar
java/projects/sftpclient/includejar/junit-4.12.jar
java/projects/sftpclient/includejar/hamcrest-core-1.3.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/bcpg-jdk15on-1.51.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/bcpkix-jdk15on-1.51.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/bcprov-jdk15on-1.51.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/slf4j-api-1.6.4.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/slf4j-jdk14-1.6.4.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/sshd-core-0.14.0.jar
java/projects/sftpclient/includejar/apache-sshd-0.14.0/apache-sshd-0.14.0/lib/tomcat-apr-5.5.23.jar
java/projects/sftpclient/includejar/commons-lang3-3.4.jar


3. die Datei java/log/config/logging.properties um folgende Zeilen ergänzen :
 
Direkt vor die Appenders  
#
log4j.logger.de.abasgmbh.utils.sftp.abas2SFTP=DEBUG, de.abasgmbh.utils.sftp.abas2SFTP.RollingFile
#

nach den Appenders : 

#
# Logging to java/log/abas2sftp.log
#de.abasgmbh.utils.sftp.abas2SFTP
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile=org.apache.log4j.RollingFileAppender
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.File=java/log/abas2sftp.log
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.Append=true
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.MaxBackupIndex=10
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.layout=org.apache.log4j.PatternLayout
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.MaxFileSize=5MB
log4j.appender.de.abasgmbh.utils.sftp.abas2SFTP.RollingFile.layout.ConversionPattern=[%X{USER_CODE}:%X{CALLER_ID}] %d{dd.MMM.yyyy HH:mm} %7r [%25.25M] %-5p %-15.15c{1} %x - %m%n
#

4. das Beispiel spx-File in den Spx-Pfad kopieren
 