# Aggregation Log Filter

The filter plugs into a logging framework and measures logging volume using incrementing counters. These counters are periodically sent to a storage backend, typically a time series database, to monitor and alert on error levels, both for the entire application as well as for individual loggers. The filter can also send a subset of log events to the database to facilitate root-cause analysis.

The filter consists of the core library and adapters implemented for supported logging frameworks.

## Supported Logging Frameworks

- [Logback](http://logback.qos.ch/documentation.html) 0.9.21+, 1.0.x, 1.1.x (slf4j 1.6.0+) - [aggregation-log-filter-logback](https://github.com/axibase/aggregation-log-filter-logback).
- [Log4j](http://logging.apache.org/log4j) 1.2.13+ - [aggregation-log-filter-log4j](https://github.com/axibase/aggregation-log-filter-log4j). 
- [Log4j2](http://logging.apache.org/log4j/2.0/) 2.5+ - [aggregation-log-filter-log4j2](https://github.com/axibase/aggregation-log-filter-log4j2). 

## Supported Time Series Databases

- [Axibase Time Series Database][atsd]

## Configuration Examples

- [Logback XML](#logback-xml-configuration-example)
- [log4j Properties](#log4j-properties-example)
- [log4j XML](#log4j-xml-example)
- [log4j2 XML](#log4j2-xml-example)

## Portal Examples

- Standalone Java Application: https://apps.axibase.com/chartlab/2f607d1b/7
- Distributed Java Application (multiple applications on different hosts): https://apps.axibase.com/chartlab/007721aa

## Installation

### Option 1: Maven

Add Maven dependency to one of logging adapters used by your application (logback, log4j, log4j2). Dependency to aggregator core will be imported automatically:

```xml
<dependency>
            <groupId>com.axibase</groupId>
            <artifactId>aggregation-log-filter-logback</artifactId>
            <version>1.0.3</version>
</dependency>
```

### Option 2: Classpath

Add both core and an adapter libraries to classpath:

- Download aggregation-log-filter-1.0.3.jar from [Maven Central](http://search.maven.org/#search|gav|1|g%3A%22com.axibase%22%20AND%20a%3A%22aggregation-log-filter%22)
- Download aggregation-log-filter-logback-1.0.3.jar from [Maven Central](http://search.maven.org/#search|gav|1|g%3A%22com.axibase%22%20AND%20a%3A%22aggregation-log-filter-logback%22)
- Copy aggregation-log-filter-1.0.3.jar and aggregation-log-filter-logback-1.0.3.jar files to lib directory. Make sure your launch script adds all jar files in lib directory, alternatively add its absolute path to classpath manually, for example:

```
java -classpath lib/app.jar:lib/aggregation-log-filter-1.0.3.jar:lib/aggregation-log-filter-logback-1.0.3.jar Main
```

## Logback XML Configuration Example

```xml 
       <filter class="com.axibase.tsd.collector.logback.Collector">
            <writer class="com.axibase.tsd.collector.writer.TcpAtsdWriter">
                <host>database_hostname</host>
                <port>8081</port>
            </writer>
        </filter>
```

  - [View logback.xml example with RollingFileAppender.](https://github.com/axibase/aggregation-log-filter-logback/blob/master/src/test/resources/logback-atsd-example.xml)

## Log4j Properties Example 

```properties
log4j.appender.APPENDER.layout=org.apache.log4j.PatternLayout
log4j.appender.APPENDER.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
log4j.appender.APPENDER.filter.COLLECTOR=com.axibase.tsd.collector.log4j.Log4jCollector
log4j.appender.APPENDER.filter.COLLECTOR.writer=tcp
log4j.appender.APPENDER.filter.COLLECTOR.writerHost=database_hostname
log4j.appender.APPENDER.filter.COLLECTOR.writerPort=8081
```

  - [View log4j.properties example.](https://github.com/axibase/aggregation-log-filter-log4j/blob/master/src/test/resources/log4j-test.properties)
 
## Log4j XML Example

```xml
    <appender name="APPENDER" class="org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d [%t] %-5p %c - %m%n"/>
        </layout>
        <filter class="com.axibase.tsd.collector.log4j.Log4jCollector">
            <param name="writer" value="tcp"/>
            <param name="writerHost" value="database_hostname"/>
            <param name="writerPort" value="8081"/>
        </filter>
    </appender>
```

  - [View complete log4j.xml example.](https://github.com/axibase/aggregation-log-filter-log4j/blob/master/src/test/resources/log4j-test.xml)

## Log4j2 XML Example

```xml
    <Appenders>
        <Console name="APPENDER">
            <PatternLayout pattern="%d [%t] %-5p %c - %m%n"/>
            <Filters>
                <Collector
                        writer="tcp"
                        writerHost="database_hostname"
                        writerPort="8081"
                        />
            </Filters>
        </Console>
        <Counter name="COUNTER"/>
    </Appenders>
```

## Adding MDC Context Parameters to Messages

### Java Example

```java
   #MDC.put("job_name", job.getName());
   MDC.put("job_name", "snmp-prd-router");
```

```
%X{key} placeholder is replaced in message pattern based on MDC context parameters
%m [%X{job_name}] is replaced to Job failed [snmp-prd-router]
```

### Log4j

```
   log4j.appender.APPENDER.filter.COLLECTOR.pattern=%m [%X{job_name}]%n
```

### Logback

```xml
 <pattern>%m [%X{job_name}]%n</pattern>
```

  - See also [Logback:Mapped Diagnostic Context](http://logback.qos.ch/manual/mdc.html)

## Configuration Settings

| Name | Required | Default | Description |
|---|---|---|---|
| writer | yes | - | see `writer` config |
| level | no | INFO | minimum level to process events |
| entity | no | current hostname | entity name for series and messages, for example application name or hostname |
| tag | no | - | user-defined tag(s) to be included in series and message commands, MULTIPLE |
| sendSeries | no | - | see `sendSeries` config |
| sendMessage | no | - | see `sendMessage` config, MULTIPLE |
| pattern | no | %m | pattern to format logging events sent to the database |

## writer

Configures a TCP, UDP or HTTP writer to send statistics and messages to a supported time series database.

### TCP writer

```xml
<writer class="com.axibase.tsd.collector.writer.TcpAtsdWriter">
    <host>database_hostname</host>
    <port>8081</port>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---| 
| host | yes | - | database hostname or IP address, string |
| port | yes | - | database TCP port, integer |

### UDP writer

```xml
<writer class="com.axibase.tsd.collector.writer.UdpAtsdWriter">
    <host>database_hostname</host>
    <port>8082</port>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---|
| host | yes | - | database hostname or IP address, string |
| port | yes | - | database UDP port, integer |

### HTTP writer

```xml
<writer class="com.axibase.tsd.collector.writer.HttpAtsdWriter">
    <url>http://database_hostname:8088/api/v1/commands/batch</url>
    <username>USERNAME</username>
    <password>PASSWORD</password>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---|
| url | yes | - | API command URL 'http://database_hostname:8088/api/v1/commands/batch', string |
| username | yes | - | username, string |
| password | yes | - | password, string |

## sendSeries

Configures how often counter and rate statistics are sent to the storage system.

```xml
<sendSeries>
    <intervalSeconds>60</intervalSeconds>
    <repeatCount>1</repeatCount>    
    <minIntervalThreshold>0</minIntervalThreshold>
    <minIntervalSeconds>5</minIntervalSeconds>
</sendSeries>
```

| Name | Required | Default Value | Description |
|---|---|---|---|
| intervalSeconds | no | 60 | Interval in seconds for sending collected log statistics |
| minIntervalSeconds | no | 5 | Minimum interval between sending of statistics (seconds), in case `minIntervalThreshold` is triggered|
| minIntervalThreshold | no | 0 | Initiates sending of statistics ahead of schedule if number of messages exceeds minIntervalThreshold |
| repeatCount | no | 1 | Maximum number of repeat values for each counter to send |
| metricPrefix | no | log_event  | Metric name prefix  |
| rateIntervalSeconds | no | 60 | Interval for rate calculation |

## sendMessage

Configures which log events should be sent to the storage system.

```xml
<sendMessage>
    <level>WARN</level>
</sendMessage>
<sendMessage>
    <level>ERROR</level>
    <stackTraceLines>15</stackTraceLines>
    <sendMultiplier>3</sendMultiplier>
    <resetIntervalSeconds>600</resetIntervalSeconds>
</sendMessage>
```

| Name | Required | Default Value | Description |
|---|---|---|---|
| level | no | WARN | Minimum trace level to which this configuration applies. For example, WARN applies to WARN, ERROR, and FATAL events. |
| stackTraceLines | no | 0 | number of stacktrace lines that will be included in the message, -1 -- all lines |
| sendMultiplier | no | ERROR: 2.0; WARN: 3.0; INFO: 5.0 | Determines how many events are sent within each interval, determined with resetIntervalSeconds; sendMultiplier = 2 : send events 1, 2, 4, 8, 16, … ; sendMultiplier = 3 : send events 1, 3, 9, 27, 81, …; sendMultiplier = M : send events 1, M, M*M,…,M^n,… |
| resetIntervalSeconds | no | 600 | Interval after which the event count is reset. |

## Log Counter Portal Example

![Log Counter Portal](https://axibase.com/wp-content/uploads/2015/11/log_filter.png)

[atsd]:https://axibase.com/products/axibase-time-series-database/
