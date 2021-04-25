import io.circe.generic.semiauto._
import io.circe.Codec

case class NameSpace(value: String) extends AnyVal

case class TweetId(value: String)
case class Tweet(id: TweetId, content: String)

enum Tracking(val id: TweetId, val ts: Long):
  case View(t: TweetId, timestamp: Long)       extends Tracking(t, timestamp)
  case Impression(t: TweetId, timestamp: Long) extends Tracking(t, timestamp)

object Tracking:
  given Namer[Tracking] with
    def name = "Tracking"

  given codec: Codec[Tracking] = deriveCodec[Tracking]

  given Record[Tracking] with
    def topic                  = Namer[Tracking].name.toLowerCase
    def key(t: Tracking)       = t.id.value
    def timestamp(t: Tracking) = t.ts

case class TweetStats(id: TweetId, ts: Long, views: Long, impressions: Long)

object TweetStats:
  def zero(id: TweetId): TweetStats = TweetStats(id, 0, 0, 0)

  given Namer[TweetStats] with
    def name = "Stats"

  given codec: Codec[TweetStats] = deriveCodec[TweetStats]

  given Record[TweetStats] with
    def topic                    = Namer[TweetStats].name.toLowerCase
    def key(t: TweetStats)       = t.id.value
    def timestamp(t: TweetStats) = t.ts

end TweetStats
