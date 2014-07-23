## Project Description
A fat-tree initializer and simple group feature test code based on ryu controller

## Usage
### Compile the Java Source Files
In order to run the script, first need to compile the corresponding Java Source Files in src directory.
Use ant source builder to compile the entire source, a target directory will be generated.

	ant
    
Note that to compile the Java Source, you need to install the ant first.

### Initialize the fat-tree topology

	./run.sh

### Execute ryu applications

	ryu-manager ryu_sample.py
