#!/bin/bash
#Created by probyoo
set -x #echo on

USER_NAME=simjunbo
DATA_BASE=/Users/$USER_NAME/var
LOCAL_BASE=/Users/$USER_NAME/lib

DOWNLOAD_AND_INSTALL_ZOOKEEPER(){
  ZOOKEEPER_BASE=$LOCAL_BASE/zookeeper
  mkdir -p $ZOOKEEPER_BASE
  cd $ZOOKEEPER_BASE
  curl -O http://apache.tt.co.kr/zookeeper/stable/zookeeper-3.4.14.tar.gz
  tar xfz ./zookeeper-3.4.14.tar.gz
  mv zookeeper-3.4.14 3.4.14
  ln -s 3.4.14 default
}

DOWNLOAD_AND_INSTALL_ZOOKEEPER

MAKE_ZOOKEEPER_INSTANCE(){
  INSTANCE_CNT=3
  CLI_PORT=2181
  PEER_IN=2888
  PEER_OUT=3888

  SERVERS=""
  for i in $(seq 1 $INSTANCE_CNT);
  do
  ZOO_N=$DATA_BASE/zookeeper/zk$i
  ZOO_CONF=$ZOO_N/conf
  ZOO_DATA=$ZOO_N/data
  ZOO_LOG=$ZOO_N/log
  ZOO_BIN=$ZOO_N/bin

  mkdir -p $ZOO_CONF
  mkdir -p $ZOO_DATA
  mkdir -p $ZOO_LOG
  mkdir -p $ZOO_BIN

  cp $ZOOKEEPER_BASE/default/conf/*.cfg $ZOO_CONF/zoo.cfg
  let N_CLI_PORT=$CLI_PORT+$i-1
  let N_PEER_IN=$PEER_IN+$i-1
  let N_PEER_OUT=$PEER_OUT+$i-1

  sed -i -e 's/2181/'${N_CLI_PORT}'/g' $ZOO_CONF/zoo.cfg
  sed -i -e 's+/tmp/zookeeper+'${ZOO_DATA}'+g' $ZOO_CONF/zoo.cfg

  echo $i > $ZOO_DATA/myid
  echo '#!/bin/bash\nexport ZOO_LOG_DIR='$ZOO_LOG > $ZOO_CONF/zookeeper-env.sh
  echo '#!/bin/bash\nexport ZOOCFGDIR='$ZOO_CONF'\n'$ZOOKEEPER_BASE'/default/bin/zkServer.sh $@' > $ZOO_BIN/zkServer.sh
  chmod 755 $ZOO_CONF/zookeeper-env.sh
  chmod 755 $ZOO_BIN/zkServer.sh

  SERVERS="${SERVERS}server.${i}=localhost:${N_PEER_IN}:${N_PEER_OUT}\n"
  done

  echo '#!/bin/bash\n' > $DATA_BASE/zookeeper/zkServerAll.sh
  chmod 755 $DATA_BASE/zookeeper/zkServerAll.sh
  for i in $(seq 1 $INSTANCE_CNT);
  do

    ZOO_N=$DATA_BASE/zookeeper/zk$i
    ZOO_CONF=$ZOO_N/conf
    ZOO_BIN=$ZOO_N/bin
    echo $ZOO_BIN'/zkServer.sh $@' >> $DATA_BASE/zookeeper/zkServerAll.sh
    echo $SERVERS >> $ZOO_CONF/zoo.cfg
  done
}

MAKE_ZOOKEEPER_INSTANCE


RUN_ZOOKEEPER_INSTANCE(){
	$DATA_BASE/zookeeper/zkServerAll.sh start
	sleep 3
	$DATA_BASE/zookeeper/zkServerAll.sh status
}

RUN_ZOOKEEPER_INSTANCE
