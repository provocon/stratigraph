scan '60 seconds'

def appenders = []
appender('CONSOLE', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    // pattern = '%-5level %logger{25}.%msg%n'
    pattern = '%msg%n'
  }
}
appenders.add('CONSOLE')

root OFF, appenders
logger "com", INFO, appenders, false
logger "de.provocon", INFO, appenders, false
