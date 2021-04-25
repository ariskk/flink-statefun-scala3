FROM apache/flink-statefun:3.0.0
RUN mkdir -p /opt/statefun/modules/e2e
COPY module.yaml /opt/statefun/modules/e2e/
COPY flink-conf.yaml $FLINK_HOME/conf/flink-conf.yaml