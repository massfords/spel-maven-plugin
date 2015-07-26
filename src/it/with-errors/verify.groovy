import groovy.json.JsonSlurper

File dir = basedir;
def reportFile = new File( dir, "target/spel-maven-plugin/report.json" );
assert reportFile.isFile()
def contents = new JsonSlurper().parse(new FileReader(reportFile))
assert contents instanceof Map

assert contents.get("checked") == 2
assert contents.get("valid") == 1
assert contents.get("invalid") == 1