# Project Minerva for Modernization - Java Binary Instrumenter
Project Minerva for Modernization is a set of libraries to analyze Java applications using AI and provide recommendations for refactoring them into partitions, which can be starting points for microservices. This binary instrumenter component enables the instrumenting of a running Java application's classes and methods in order to collect data for AI refactoring consideration.

# Build (Maven Based)
mvn install

# Build (Docker Based)
docker build -t minerva-agent .

docker run --rm -it -v [target dir]:/var/install minerva-agent

# Usage

java -javaagent:[base dir]/minerva-agent-1.0-jar-with-dependencies.jar=[JSON-based configuration file or a directory containing instrumenter-config.json] ...

e.g. java -javaagent:/c/eclipse/minerva-agent/agent/target/minerva-agent-1.0-jar-with-dependencies.jar=/c/example/agent-config.json ...
e.g. java -javaagent:/c/eclipse/minerva-agent/agent/target/minerva-agent-1.0-jar-with-dependencies.jar=/c/example ... (if /c/example contains instrumenter-config.json)

# Configuration (JSON)

```
{
	"filter": {
		"type": (required, string :: filter type name),
		"version": (required, string :: filter version),
		"config": (optional, JSON element :: filter configuration)
	},
	"generator": {
		"type": (required, string :: generator type name),
		"version": (required, string :: generator version),
		"config": (optional, JSON element :: generator configuration)
	}
}
```

Example:

```
{
	"filter": {
		"type": "sym-ref-tables",
		"version": "1.0",
		"config": "/c/daytrader/application-data/tables"
	},
	"generator": {
		"type": "println",
		"version": "1.0"
	}
}
```

# Filter Configuration (Minerva Analyzer symTable/refTable)

```
"filter": {
	"type": "sym-ref-tables",
	"version": "1.0",
	"config": (string :: directory containing symTable/refTable JSON files)
}
```

Example:

```
"filter": {
	"type": "sym-ref-tables",
	"version": "1.0",
	"config": "/c/daytrader/application-data/tables"
}
```

# Filter Configuration (List of packages)

```
"filter": {
	"type": "package",
	"version": "1.0",
	"config": (array :: package name) or (string :: package name)
}
```

Example:

```
"filter": {
	"type": "package",
	"version": "1.0",
	"config": ["com.ibm.websphere.samples.daytrader"]
}
```

# Generator Configuration (Minerva System.out/System.err instrumentation)

```
"generator": {
	"type": "println",
	"version": "1.0"
}
```

# Generator Configuration (java.util.logging based instrumentation)

```
"generator": {
	"type": "java-util-logging",
	"version": "1.0"
}
```
