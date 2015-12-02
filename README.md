# Aggregation Log Filter

The filter plugs into a logging framework and measures the number of raised log events using incrementing counters. These counters are periodically flushed to a storage backend to monitoring error levels, both for the entire application as well as for individual loggers. The filter can be also enabled to send a small subset of log events to facilitate root-cause analysis.

## Supported Logging Frameworks

- logback 0.9.21+, 1.0.x, 1.1.x (slf4j 1.6.0+)
- log4j 1.2.13-1.2.17 

## Supported Storage Backends

- [Axibase Time-Series Database][atsd]

## Installation

### Option 1: Add Maven Dependency to your Application

```xml
<dependency>
            <groupId>com.axibase</groupId>
            <artifactId>aggregation-log-filter</artifactId>
            <version>0.4.5</version>
</dependency>
```

### Option 2: Add aggregation-log-filter to classpath

- Dowload aggregation-log-filter-0.4.5.jar from [Maven Central](http://search.maven.org/#search|gav|1|g%3A%22com.axibase%22%20AND%20a%3A%22aggregation-log-filter%22)
- Copy aggregation-log-filter-0.4.5.jar file to lib directory. Make sure your launch script adds all jar files in lib directory, alternatively add its absolute path to classpath manually, for example:

```
java -server -classpath /opt/atsd-executable.jar:/opt/aggregation-log-filter-0.4.5.jar com.axibase.tsd.Server
```

## Logback Configuration 

```xml 
       <filter class="com.axibase.tsd.collector.logback.Collector">
            <writer class="com.axibase.tsd.collector.writer.HttpStreamingAtsdWriter">
                <url>http://atsd_server:8088/api/v1/command/</url>
                <username>USERNAME</username>
                <password>PASSWORD</password>
            </writer>
            <level>INFO</level>
            <sendSeries>
                <repeatCount>3</repeatCount>
                <metricPrefix>log_event</metricPrefix>
                <intervalSeconds>60</intervalSeconds>
                <minIntervalThreshold>100</minIntervalThreshold>
                <minIntervalSeconds>1</minIntervalSeconds>
            </sendSeries>
            <sendMessage>
                <every>100</every>
            </sendMessage>
            <sendMessage>
                <level>ERROR</level>
                <every>30</every>
                <stackTraceLines>-1</stackTraceLines>
            </sendMessage>
            <tag>
                <name>CUSTOM_TAG</name>
                <value>TAG_VALUE</value>
            </tag>
        </filter>
```

| Name | Required | Default | Description |
|---|---|---|---|
| writer | yes | - | see `writer` config |
| level | no | TRACE | minimum level to process event |
| entity | no | current hostname | entity name for series and messages, for example application name or hostname |
| tag | no | - | user-defined tag(s) to be included in series and message commands, MULTIPLE |
| sendSeries | yes | - | see `sendSeries` config |
| sendMessage | no | - | see `sendMessage` config, MULTIPLE |


## writer

Configures a TCP, UDP or HTTP writer to send statistics and messages to a backend storage system such as Axibase Time-Series Database.

### TCP writer

```xml
<writer class="com.axibase.tsd.collector.writer.TcpAtsdWriter">
    <host>atsd_server</host>
    <port>8081</port>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---| 
| host | yes | - | ATSD host, string |
| port | yes | - | ATSD TCP port, integer |

### UDP writer

```xml
<writer class="com.axibase.tsd.collector.writer.UdpAtsdWriter">
    <host>atsd_server</host>
    <port>8082</port>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---|
| host | yes | - | ATSD host, string |
| port | yes | - | ATSD UDP port, integer |

### HTTP writer

```xml
<writer class="com.axibase.tsd.collector.writer.HttpStreamingAtsdWriter">
    <url>http://atsd_server:8088/api/v1/command/</url>
    <username>axibase</username>
    <password>*****</password>
    <reconnectTimeoutMs>60000</reconnectTimeoutMs>
</writer>
```

| Name | Required | Default | Description |
|---|---|---|---|
| url | yes | - | ATSD API command URL like 'http://atsd_server:8088/api/v1/command/', string |
| username | yes | - | user name, string |
| password | yes | - | password, string |

## sendSeries

Configures how often counter and rate statistics are sent to the storage system.

```xml
<sendSeries>
    <!-- default: 60 -->
    <intervalSeconds>60</intervalSeconds>
    <!-- 0+ default:1 -->
    <repeatCount>5</repeatCount>    
    <!-- default: 0 -->
    <minIntervalThreshold>0</minIntervalThreshold>
    <!-- default: 5 -->
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

## Log4j Configuration
 
### XML example

```xml
    <appender name="APPENDER" class="org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d [%t] %-5p %c - %m%n"/>
        </layout>
        <filter class="com.axibase.tsd.collector.log4j.Log4jCollector">
            <param name="writer" value="tcp"/>
            <param name="writerHost" value="localhost"/>
            <param name="writerPort" value="8081"/>
            <!--
            <param name="writer" value="http"/>
            <param name="writerUrl" value="http://atsd_server:8088/api/v1/command/"/>
            <param name="writerUsername" value="USERNAME"/>
            <param name="writerPassword" value="PASSWORD"/>
            <param name="writerReconnectTimeoutMs" value="60000"/>
            -->
            <param name="level" value="INFO"/>
            <param name="repeatCount" value="3"/>
            <param name="intervalSeconds" value="60"/>
            <param name="minIntervalSeconds" value="5"/>
            <param name="minIntervalThreshold" value="100"/>
            <param name="messages" value="WARN;ERROR=-1"/>
        </filter>
    </appender>
```
 
### Properties example 

```properties
log4j.appender.APPENDER.layout=org.apache.log4j.PatternLayout
log4j.appender.APPENDER.layout.ConversionPattern=%d [%t] %-5p %c - %m%n

log4j.appender.APPENDER.filter=com.axibase.tsd.collector.log4j.Log4jCollector
log4j.appender.APPENDER.filter.COLLECTOR.writer=tcp
log4j.appender.APPENDER.filter.COLLECTOR.writerHost=localhost
log4j.appender.APPENDER.filter.COLLECTOR.writerPort=8081
#log4j.appender.APPENDER.filter.COLLECTOR.writer=HTTP
#log4j.appender.APPENDER.filter.COLLECTOR.writerUrl=http://atsd_server:8088/api/v1/command/
#log4j.appender.APPENDER.filter.COLLECTOR.writerUsername=USERNAME
#log4j.appender.APPENDER.filter.COLLECTOR.writerPassword=PASSWORD
#log4j.appender.APPENDER.filter.COLLECTOR.writerReconnectTimeoutMs=60000
log4j.appender.APPENDER.filter.COLLECTOR.level=INFO
log4j.appender.APPENDER.filter.COLLECTOR.repeatCount=3
log4j.appender.APPENDER.filter.COLLECTOR.intervalSeconds=60
log4j.appender.APPENDER.filter.COLLECTOR.minIntervalSeconds=5
log4j.appender.APPENDER.filter.COLLECTOR.minIntervalThreshold=100
log4j.appender.APPENDER.filter.COLLECTOR.messages=WARN;ERROR=-1
```

## Log Counter Portal Example

![Log Counter Portal](https://axibase.com/wp-content/uploads/2015/11/log_filter.png)
Format: ![Alt Text](url)

[atsd]:https://axibase.com/products/axibase-time-series-database/
