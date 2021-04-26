package com.ariskk.statefun3

import munit.FunSuite
import org.testcontainers.containers.Network
import org.testcontainers.Testcontainers

trait IntetrationTestSupport extends FunSuite with FlinkSupport with KafkaSupport:

  val network = Network.newNetwork
  val kafka = kafkaContainer(network)
  val flinkMaster = flinkMasterContainer(network)
  val flinkWorker = flinkWorkerContainer(network)
  
  val server = buildServer

  val withFixtures = FunFixture[String](
    setup = { _ =>
      // Start server
      server.start
      kafka.start
      // Map server port
      Testcontainers.exposeHostPorts(5000)
      // Start all containers
      flinkMaster.start
      flinkWorker.start

      kafka.getBootstrapServers()
    },
    teardown = { _ =>
      // Stop all containers
      flinkWorker.stop
      flinkMaster.stop
      kafka.stop
      server.stop
    }
  )

end IntetrationTestSupport