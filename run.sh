#!/bin/sh
pkill -9 ovsdb-server
pkill -9 ovs-vsctl
pkill -9 ovs-vswitchd
rm -f /usr/local/var/run/openvswitch/*

ovsdb-server /usr/local/etc/openvswitch/conf.db --remote=punix:/usr/local/var/run/openvswitch/db.sock --pidfile --detach
ovs-vsctl --no-wait init
ovs-vswitchd --pidfile --detach

mn --controller=remote,ip=127.0.0.1 --custom fat-tree-topo.py --topo=mytopo
