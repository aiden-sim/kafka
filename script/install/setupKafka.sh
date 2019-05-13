#!/bin/bash
set -x #echo on

USER_NAME=simjunbo

DATA_BASE=/Users/$USER_NAME/var
LOCAL_BASE=/Users/$USER_NAME/lib


DOWNLOAD_AND_INSTALL_KAFKA(){
  KAFKA_BASE=$LOCAL_BASE/kafka
  mkdir -p $KAFKA_BASE
  cd $KAFKA_BASE
  curl -O http://mirror.navercorp.com/apache/kafka/2.2.0/kafka_2.12-2.2.0.tgz
  tar xfz ./kafka_2.12-2.2.0.tgz
  mv kafka_2.12-2.2.0 2.2.0
  ln -s 2.2.0 default
}

DOWNLOAD_AND_INSTALL_KAFKA


MAKE_KAFKA_INSTANCE(){
  INSTANCE_CNT=3
  ZOOKEEPER_SERVERS=localhost:2181,localhost:2182,localhost:2183/kafka_C01
  PORT=9092

  mkdir -p $DATA_BASE/kafka
  echo '#!/bin/bash\n' > $DATA_BASE/kafka/kafkaServerAll.sh
  chmod 755 $DATA_BASE/kafka/kafkaServerAll.sh

  for i in $(seq 1 $INSTANCE_CNT);
  do
    KAFKA_N=$DATA_BASE/kafka/kafka$i
    KAFKA_N_CONF=$KAFKA_N/conf
    KAFKA_N_LOG=$KAFKA_N/log
    KAFKA_N_BIN=$KAFKA_N/bin
    mkdir -p $KAFKA_N
    mkdir -p $KAFKA_N_CONF
    mkdir -p $KAFKA_N_LOG
    mkdir -p $KAFKA_N_BIN

    cp $KAFKA_BASE/default/config/server.properties $KAFKA_N_CONF/server.properties

    let PORT_N=$PORT+$i-1
    sed -i -e 's+#listeners=PLAINTEXT://:9092+listeners=PLAINTEXT://localhost:'${PORT_N}'+g' $KAFKA_N_CONF/server.properties
    sed -i -e 's/broker.id=0/broker.id='$i'/g' $KAFKA_N_CONF/server.properties
    sed -i -e 's+/tmp/kafka-logs+'$KAFKA_N_LOG'+g' $KAFKA_N_CONF/server.properties
    sed -i -e 's+localhost:2181+'$ZOOKEEPER_SERVERS'+g' $KAFKA_N_CONF/server.properties


    echo '#!/bin/bash\nexport KAFKA_HEAP_OPTS="-Xmx256M -Xms256M"\ncd '$KAFKA_BASE'/default/bin\n./kafka-server-start.sh -daemon '$KAFKA_N_CONF'/server.properties' > $KAFKA_N_BIN/kafka-server-start.sh
    chmod 755 $KAFKA_N_BIN/kafka-server-start.sh
    echo $KAFKA_N_BIN'/kafka-server-start.sh' >> $DATA_BASE/kafka/kafkaServerAll.sh
  done
}

MAKE_KAFKA_INSTANCE

$DATA_BASE/kafka/kafkaServerAll.sh
