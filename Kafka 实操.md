## Kafka 实操

由于kafka使用zookeeper，所以首先要启动zookeeper

使用kafaka自带zookeeper来启动自带zookeeper脚本：

`> bin/zookeeper-server-start.sh config/zookeeper.properties`

同时也可使用zookeeper启动独立zookeeper命令：

`> ./zkServer.sh start`

启动kafka服务：

`> bin/kafka-server-start.sh config/server.properties`



创建一个topic：

`> bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test`



list topic:

`> bin/kafka-topics.sh --list --bootstrap-server localhost:9092`



生成数据：

`> bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test`



消费数据：

`> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning`

