#!/bin/sh
MY_PATH=$(dirname $0)
java -classpath "$MY_PATH/lib/*:$MY_PATH/target/bin" edu.yust.cecom.OvsRouteTableMod $1 $2 $3
