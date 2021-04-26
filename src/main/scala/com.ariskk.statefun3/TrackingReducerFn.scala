package com.ariskk.statefun3

import org.apache.flink.statefun.sdk.java.Context
import org.apache.flink.statefun.sdk.java.message.Message

final class TrackingReducerFn(using ns: NameSpace) extends StateFun[Tracking, TweetStats, TweetStats]:

  val functionName = "tracking-reducer"

  def process: (Tracking, Option[TweetStats]) => (TweetStats, TweetStats) = { (tracking, state) =>
    val currentState = state.getOrElse(TweetStats.zero(tracking.id))
    val updated = tracking match {
      case Tracking.Impression(id, ts) =>
        currentState.copy(ts = ts, impressions = currentState.impressions + 1)
      case Tracking.View(id, ts) =>
        currentState.copy(ts = ts, views = currentState.views + 1)
    }
    (updated, updated)
  }

end TrackingReducerFn

object TrackingReducerFn:
  def apply(using ns: NameSpace): TrackingReducerFn = new TrackingReducerFn
