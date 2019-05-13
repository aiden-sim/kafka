#!/bin/bash
set -x #echo on

LOCAL_BASE=/Users/simjunbo/lib

DOWNLOAD_AND_INSTALL_KAFKA_MANAGER(){
   KAFKA_MANAGER_BASE=$LOCAL_BASE/kafka-manager
   mkdir -p $KAFKA_MANAGER_BASE
   cd $KAFKA_MANAGER_BASE
   curl -O -L https://github.com/yahoo/kafka-manager/archive/2.0.0.2.tar.gz
   tar xfz ./2.0.0.2.tar.gz
   mv kafka-manager-2.0.0.2 ./src
}

DOWNLOAD_AND_INSTALL_KAFKA_MANAGER

MAKE_KAFKA_MANAGER_INSTANCE(){
  ZOOKEEPER_SERVERS=localhost:2181,localhost:2182,localhost:2183/kafka_C01
  sed -i -e 's+kafka-manager-zookeeper:2181+'$ZOOKEEPER_SERVERS'+g' $KAFKA_MANAGER_BASE/src/conf/application.conf

  cd $KAFKA_MANAGER_BASE/src
  ./sbt clean dist
  # 회사의 경우 SSL 인증관련 오류 발생.
  # $JAVA_HOME/jre/lib/security/jssecacerts 에 host 추가 참고 https://groups.google.com/forum/#!topic/pinpoint_user/8nyymde8XSo
  if [ -f $KAFKA_MANAGER_BASE/src/target/universal/kafka-manager-2.0.0.2.zip ]
  then
    mv $KAFKA_MANAGER_BASE/src/target/universal/kafka-manager-2.0.0.2.zip $KAFKA_MANAGER_BASE/
    cd $KAFKA_MANAGER_BASE
    unzip kafka-manager-2.0.0.2.zip
    mv kafka-manager-2.0.0.2 ./2.0.0.2
    ln -s 2.0.0.2 default
  else
  	echo "Build Fail"
    echo "회사의 경우 SSL 인증관련 오류 발생. "
    echo "$JAVA_HOME/jre/lib/security/jssecacerts 에 host 추가. "
    echo "참고:  https://groups.google.com/forum/#!topic/pinpoint_user/8nyymde8XSo"
  fi
}

MAKE_KAFKA_MANAGER_INSTANCE
