# Stratigraph Softwarepackage Layer Analyser

Stratigraph is at the moment is a small command line tools to automatically
detect the interconnection of soure code packages and the resulting layering
of the overall system.

It can be used to check, if the code analysed follows a layering structure and
if the intended structure matches the one derived from the code.


## Feedback

Please use the [issues][issues] section of this repository at [github][github] 
for feedback. 


## Results

Stratigraph produces a graph of the mutual usages of source code packages of a 
software system and derives a layering from this graph, where some packages form
the lowest layer of functionality not used by any other layer. Higher layers
of the system are defined by the usage of lower layers down to the first layer.

In these analysis runs certain packages can be left unconcidered or other
packages might be combined for a more coarse view if needed.


## Supported Languages

* Java

## Download of Packages

The latest packages ready to use can be found at the [build pipeline](https://gitlab.com/provocon/stratigraph/pipelines)
at [GitLab][gitlab]. Just download the build artifacts fro the last `passed`
build.


## Usage

Call the start script with a source code directory as the first parameter:

```
stg ~/proj/base
```

## Unachived Ultimate Goal and Visualizations

The original goal was to analyse the package structure of a software system
and derive a visualization of the mutual usage of packages illustrating the
layering of the overall system.

It turned out that the automated placement provided by a set of libraries
tried doesn't meet the expectation to at least get a decent starting point
for some later manual refinements.

The leftovers from this ultimate goal can be found in the graph visualization
options on the command line, which still can be used to trigger graphical
windows to be opened with a view of the software structure using different
graph libraries.

## Building

Stratigraph itself is a Java program which can be built with the [Gradle][gradle]
build tool.

```
./gradlew assembleDist
```

The full test cycle can be achieved - like CI does it - with

```
./gradlew jacocoTestReport audit build assemble
```

[issues]: https://github.com/provocon/stratigraph/issues
[github]: https://github.com/provocon/stratigraph
[gitlab]: https://gitlab.com/provocon/stratigraph
[gradle]: https://gradle.org/
