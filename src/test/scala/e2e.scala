
class E2ETest extends IntetrationTestSupport:

  withFixtures.test("Counting Tweet Stats") { kafkaBrokers =>
    val kafkaProducer = producer(kafkaBrokers)
    val kafkaConsumer = consumer(kafkaBrokers)

    (1 to 100).foreach(i =>
      kafkaProducer.send[Tracking](
        Tracking.Impression(TweetId(s"tw1-${i % 10}"), System.currentTimeMillis)
      )
    )

    (1 to 20).foreach(i =>
      kafkaProducer.send[Tracking](
        Tracking.View(TweetId(s"tw1-${i % 10}"), System.currentTimeMillis)
      )
    )

    val allStats = kafkaConsumer.consumeAll[TweetStats]
    
    val lastStats = allStats.groupBy(_.id).mapValues(_.last)

    assert(lastStats.forall((_, v) => v.views == 2 && v.impressions == 10))

  }

end E2ETest