net stop tomcat8
del /F /Q D:\Tomcat8\webapps\xonmapservice.war
del /F /S /Q D:\Tomcat8\webapps\xonmapservice
copy .\build\libs\XonMapService-0.1.war D:\Tomcat8\webapps\xonmapservice.war
net start tomcat8