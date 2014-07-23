## Project Description
A fat-tree initializer and simple group feature test code based on ryu controller

## Usage
### Compile the Java Source Files for default fat-tree routing
In order to run the default fat-tree routing, first need to compile the corresponding Java Source Files in src directory.
Use ant source builder to compile the entire source, a target directory will be generated.

	cd routing
	ant
	./modFlowTablesByOvs.sh add k
    
Note that to compile the Java Source, you need to install the ant first.
And k here denotes the fat-tree's parameter k. As default you can use 4.

### Initialize the fat-tree topology

	./run.sh

### Execute ryu applications

	ryu-manager ryu_sample.py

### View the flow entries in a switch

	ovs-ofctl dump-flows [switch_name] -O OpenFlow13

The switch name has the format like s[pod]_[sw_idx]_1 such as s0_0_1