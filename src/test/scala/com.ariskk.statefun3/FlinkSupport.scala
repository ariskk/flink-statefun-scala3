package com.ariskk.statefun3

import java.util.function.Consumer
import java.nio.file.Path
import java.nio.file.Paths

import org.testcontainers.utility.DockerImageName
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.BindMode

final class FlinkContainer(image: ImageFromDockerfile) extends GenericContainer[FlinkContainer](image)

trait FlinkSupport:

  def image(name: String) = 
    new ImageFromDockerfile(s"statefun-${name}")
      .withDockerfile(Paths.get("src/test/resources/Dockerfile"))

  val MasterHost = "flink-master"
  val WokrerHost = "flink-worker"

  def flinkMasterContainer(network: Network) = new FlinkContainer(image("master"))
    .withNetworkAliases(MasterHost)
    .withNetwork(network)
    .withEnv("ROLE", "master")
    .withEnv("MASTER_HOST", MasterHost)
    .withCommand("-p 1")
    .withExposedPorts(8081)

  def flinkWorkerContainer(network: Network) = new FlinkContainer(image("worker"))
    .withNetworkAliases(WokrerHost)
    .withNetwork(network)
    .withEnv("ROLE", "worker")
    .withEnv("MASTER_HOST", MasterHost)

end FlinkSupport