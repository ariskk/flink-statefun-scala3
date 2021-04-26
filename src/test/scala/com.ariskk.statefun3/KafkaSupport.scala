package com.ariskk.statefun3

import java.util.Properties
import java.time.Duration
import scala.util.Random
import scala.collection.JavaConverters._

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.Network
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{ProducerConfig, KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringSerializer, StringDeserializer}
import io.circe.Codec
import io.circe.parser._

trait KafkaSupport:

  val KafkaHost = "kafka-broker"

  def kafkaContainer(network: Network) = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1"))
    .withNetworkAliases(KafkaHost)
    .withNetwork(network)
    .withEnv("KAFKA_CREATE_TOPICS","tracking:1:1,stats:1:1")

  def producer(bootstrapServers: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

    new KafkaProducer[String, String](props)
  }

  extension (producer: KafkaProducer[String, String])
    def send[T: Record: Codec](t: T) =
      producer.send(new ProducerRecord(
        Record[T].topic, 
        null, 
        Record[T].timestamp(t), 
        Record[T].key(t), 
        Record[T].payload(t)
      ))

  def consumer(bootstrapServers: String, groupId: String = s"test-${Random.nextInt(100000)}") = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.deserializer", classOf[StringDeserializer])
    props.put("value.deserializer", classOf[StringDeserializer])
    props.put("group.id", groupId)
    props.put("auto.offset.reset", "earliest")

    new KafkaConsumer[String, String](props)
  }

  extension (consumer: KafkaConsumer[String, String])
    def consumeAll[T: Record: Codec]: List[T] = {
      consumer.subscribe(Seq(Record[T].topic).asJava)
      Iterator
        .continually(consumer.poll(Duration.ofSeconds(10)))
        .takeWhile(!_.isEmpty)
        .flatMap(_.iterator.asScala)
        .flatMap { record =>
          parse(record.value).flatMap(json =>
            Codec[T].apply(json.hcursor)
          ).toSeq
        }.toList
    }


  
end KafkaSupport