import groovy.json.JsonSlurper
File dir = basedir
def reportFile = new File( dir, "target/spel-maven-plugin/report.txt" );
assert reportFile.isFile()
def contents = new JsonSlurper().parseText(reportFile.text)
assert contents instanceof Map

assert contents.getProperty("checked") == properties.getProperty("spel-annotations-amount")