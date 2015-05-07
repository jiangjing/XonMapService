@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  XonMapService startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and XON_MAP_SERVICE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\XonMapService-0.1.jar;%APP_HOME%\lib\spring-boot-starter-logging-1.2.3.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-actuator-1.2.3.RELEASE.jar;%APP_HOME%\lib\spring-boot-autoconfigure-1.2.3.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-1.2.3.RELEASE.jar;%APP_HOME%\lib\grails-web-boot-3.0.1.jar;%APP_HOME%\lib\hibernate-4.3.8.0.jar;%APP_HOME%\lib\cache-3.0.1.jar;%APP_HOME%\lib\hibernate-ehcache-4.3.8.Final.jar;%APP_HOME%\lib\asset-pipeline-3.0.1.jar;%APP_HOME%\lib\scaffolding-3.0.2.jar;%APP_HOME%\lib\mysql-connector-java-5.1.35.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.11.jar;%APP_HOME%\lib\jul-to-slf4j-1.7.11.jar;%APP_HOME%\lib\log4j-over-slf4j-1.7.11.jar;%APP_HOME%\lib\logback-classic-1.1.3.jar;%APP_HOME%\lib\spring-boot-starter-1.2.3.RELEASE.jar;%APP_HOME%\lib\spring-boot-actuator-1.2.3.RELEASE.jar;%APP_HOME%\lib\spring-core-4.1.6.RELEASE.jar;%APP_HOME%\lib\spring-boot-1.2.3.RELEASE.jar;%APP_HOME%\lib\snakeyaml-1.14.jar;%APP_HOME%\lib\tomcat-embed-core-8.0.20.jar;%APP_HOME%\lib\tomcat-embed-el-8.0.20.jar;%APP_HOME%\lib\tomcat-embed-logging-juli-8.0.20.jar;%APP_HOME%\lib\tomcat-embed-websocket-8.0.20.jar;%APP_HOME%\lib\grails-bootstrap-3.0.1.jar;%APP_HOME%\lib\grails-plugin-rest-3.0.1.jar;%APP_HOME%\lib\grails-plugin-databinding-3.0.1.jar;%APP_HOME%\lib\grails-plugin-i18n-3.0.1.jar;%APP_HOME%\lib\grails-plugin-filters-3.0.1.jar;%APP_HOME%\lib\grails-plugin-gsp-3.0.1.jar;%APP_HOME%\lib\grails-plugin-services-3.0.1.jar;%APP_HOME%\lib\grails-plugin-url-mappings-3.0.1.jar;%APP_HOME%\lib\grails-plugin-interceptors-3.0.1.jar;%APP_HOME%\lib\grails-plugin-async-3.0.1.jar;%APP_HOME%\lib\h2-1.3.176.jar;%APP_HOME%\lib\groovy-2.4.3.jar;%APP_HOME%\lib\grails-web-common-3.0.1.jar;%APP_HOME%\lib\grails-datastore-gorm-hibernate4-4.0.0.RELEASE.jar;%APP_HOME%\lib\concurrentlinkedhashmap-lru-1.4.jar;%APP_HOME%\lib\jboss-logging-3.1.3.GA.jar;%APP_HOME%\lib\jboss-logging-annotations-1.2.0.Beta1.jar;%APP_HOME%\lib\hibernate-core-4.3.8.Final.jar;%APP_HOME%\lib\ehcache-core-2.4.3.jar;%APP_HOME%\lib\rhino-1.7R4.jar;%APP_HOME%\lib\commons-io-2.2.jar;%APP_HOME%\lib\asset-pipeline-core-2.1.1.jar;%APP_HOME%\lib\fields-2.0.1.jar;%APP_HOME%\lib\logback-core-1.1.3.jar;%APP_HOME%\lib\jackson-databind-2.4.5.jar;%APP_HOME%\lib\spring-context-4.1.6.RELEASE.jar;%APP_HOME%\lib\groovy-xml-2.4.3.jar;%APP_HOME%\lib\grails-web-3.0.1.jar;%APP_HOME%\lib\grails-plugin-controllers-3.0.1.jar;%APP_HOME%\lib\grails-plugin-datasource-3.0.1.jar;%APP_HOME%\lib\gson-2.2.4.jar;%APP_HOME%\lib\grails-core-3.0.1.jar;%APP_HOME%\lib\commons-lang-2.6.jar;%APP_HOME%\lib\grails-plugin-codecs-3.0.1.jar;%APP_HOME%\lib\grails-logging-3.0.1.jar;%APP_HOME%\lib\grails-web-gsp-taglib-3.0.1.jar;%APP_HOME%\lib\spring-tx-4.1.6.RELEASE.jar;%APP_HOME%\lib\grails-plugin-events-3.0.1.jar;%APP_HOME%\lib\grails-validation-3.0.1.jar;%APP_HOME%\lib\grails-databinding-3.0.1.jar;%APP_HOME%\lib\grails-encoder-3.0.1.jar;%APP_HOME%\lib\grails-gsp-3.0.1.jar;%APP_HOME%\lib\groovy-templates-2.4.3.jar;%APP_HOME%\lib\spring-webmvc-4.1.6.RELEASE.jar;%APP_HOME%\lib\spring-context-support-4.1.6.RELEASE.jar;%APP_HOME%\lib\dom4j-1.6.1.jar;%APP_HOME%\lib\grails-datastore-gorm-hibernate-core-4.0.0.RELEASE.jar;%APP_HOME%\lib\hibernate-validator-5.0.3.Final.jar;%APP_HOME%\lib\jboss-transaction-api_1.2_spec-1.0.0.Final.jar;%APP_HOME%\lib\hibernate-jpa-2.1-api-1.0.0.Final.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\jandex-1.1.0.Final.jar;%APP_HOME%\lib\closure-compiler-v20141023.jar;%APP_HOME%\lib\jackson-annotations-2.4.0.jar;%APP_HOME%\lib\jackson-core-2.4.5.jar;%APP_HOME%\lib\spring-aop-4.1.6.RELEASE.jar;%APP_HOME%\lib\spring-beans-4.1.6.RELEASE.jar;%APP_HOME%\lib\spring-expression-4.1.6.RELEASE.jar;%APP_HOME%\lib\spring-aspects-4.1.6.RELEASE.jar;%APP_HOME%\lib\aspectjrt-1.8.5.jar;%APP_HOME%\lib\aspectjweaver-1.8.5.jar;%APP_HOME%\lib\grails-web-gsp-3.0.1.jar;%APP_HOME%\lib\grails-web-databinding-3.0.1.jar;%APP_HOME%\lib\grails-web-url-mappings-3.0.1.jar;%APP_HOME%\lib\grails-web-jsp-3.0.1.jar;%APP_HOME%\lib\grails-web-mvc-3.0.1.jar;%APP_HOME%\lib\grails-web-sitemesh-3.0.1.jar;%APP_HOME%\lib\grails-async-3.0.1.jar;%APP_HOME%\lib\grails-plugin-converters-3.0.1.jar;%APP_HOME%\lib\grails-plugin-mimetypes-3.0.1.jar;%APP_HOME%\lib\grails-plugin-validation-3.0.1.jar;%APP_HOME%\lib\grails-plugin-domain-class-3.0.1.jar;%APP_HOME%\lib\tomcat-embed-logging-log4j-7.0.55.jar;%APP_HOME%\lib\spring-jdbc-4.1.6.RELEASE.jar;%APP_HOME%\lib\groovy-sql-2.4.3.jar;%APP_HOME%\lib\tomcat-jdbc-7.0.55.jar;%APP_HOME%\lib\serializer-2.7.2.jar;%APP_HOME%\lib\grails-spring-3.0.1.jar;%APP_HOME%\lib\commons-codec-1.6.jar;%APP_HOME%\lib\reactor-spring-context-2.0.0.RELEASE.jar;%APP_HOME%\lib\reactor-bus-2.0.0.RELEASE.jar;%APP_HOME%\lib\commons-validator-1.4.0.jar;%APP_HOME%\lib\groovy-json-2.4.3.jar;%APP_HOME%\lib\spring-web-4.1.6.RELEASE.jar;%APP_HOME%\lib\grails-taglib-3.0.1.jar;%APP_HOME%\lib\xml-apis-1.0.b2.jar;%APP_HOME%\lib\grails-datastore-gorm-4.0.0.RELEASE.jar;%APP_HOME%\lib\grails-datastore-gorm-plugin-support-4.0.0.RELEASE.jar;%APP_HOME%\lib\grails-datastore-core-4.0.0.RELEASE.jar;%APP_HOME%\lib\spring-orm-4.1.6.RELEASE.jar;%APP_HOME%\lib\jta-1.1.jar;%APP_HOME%\lib\hibernate-commons-annotations-3.2.0.Final.jar;%APP_HOME%\lib\validation-api-1.1.0.Final.jar;%APP_HOME%\lib\classmate-1.0.0.jar;%APP_HOME%\lib\closure-compiler-externs-v20141023.jar;%APP_HOME%\lib\args4j-2.0.26.jar;%APP_HOME%\lib\guava-18.0.jar;%APP_HOME%\lib\protobuf-java-2.5.0.jar;%APP_HOME%\lib\jsr305-1.3.9.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\grails-web-taglib-3.0.1.jar;%APP_HOME%\lib\sitemesh-2.4.jar;%APP_HOME%\lib\gpars-1.2.1.jar;%APP_HOME%\lib\reactor-core-2.0.0.RELEASE.jar;%APP_HOME%\lib\reactor-stream-2.0.0.RELEASE.jar;%APP_HOME%\lib\grails-datastore-simple-4.0.0.RELEASE.jar;%APP_HOME%\lib\tomcat-juli-7.0.55.jar;%APP_HOME%\lib\groovy-ant-2.4.3.jar;%APP_HOME%\lib\json-path-0.9.0.jar;%APP_HOME%\lib\reactor-spring-core-2.0.0.RELEASE.jar;%APP_HOME%\lib\gs-collections-5.1.0.jar;%APP_HOME%\lib\grails-datastore-web-4.0.0.RELEASE.jar;%APP_HOME%\lib\jsr166y-1.7.0.jar;%APP_HOME%\lib\disruptor-3.3.2.jar;%APP_HOME%\lib\reactive-streams-1.0.0.RC3.jar;%APP_HOME%\lib\groovy-groovydoc-2.4.3.jar;%APP_HOME%\lib\ant-1.9.4.jar;%APP_HOME%\lib\ant-antlr-1.9.4.jar;%APP_HOME%\lib\ant-launcher-1.9.4.jar;%APP_HOME%\lib\ant-junit-1.9.4.jar;%APP_HOME%\lib\json-smart-1.2.jar;%APP_HOME%\lib\gs-collections-api-5.1.0.jar;%APP_HOME%\lib\slf4j-api-1.7.11.jar;%APP_HOME%\lib\hibernate-commons-annotations-4.0.5.Final.jar;%APP_HOME%\lib\javassist-3.18.1-GA.jar;%APP_HOME%\lib\commons-logging-1.2.jar

@rem Execute XonMapService
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %XON_MAP_SERVICE_OPTS%  -classpath "%CLASSPATH%" com.xonmap.Application %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable XON_MAP_SERVICE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%XON_MAP_SERVICE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
