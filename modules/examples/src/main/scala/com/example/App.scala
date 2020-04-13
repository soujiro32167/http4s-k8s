package com.example

import com.example.ClientModule.ClientModule
import com.example.K8sConfigModule.K8sConfigModule
import io.sk8s.client.core_v1.Core_v1Client
import org.http4s.client.Client
import zio._
import zio.console.{Console, _}
import zio.interop.catz._
import io.circe.syntax._



object App extends zio.App {
  private val log = org.log4s.getLogger

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    appLogic
        .tapError(t => Task(log.error(t)("")))
      .provideLayer(liveEnv).fold(_ => 1, _ => 0 )
  }

  type AppEnv = Console with ClientModule with K8sConfigModule

  val liveEnv: ZLayer[zio.ZEnv, Throwable, AppEnv] = ZEnv.live ++ K8sConfigModule.live >>> ClientModule.live.passthrough

  val nopTrace: String => Client[Task] => Client[Task] = _ => c => c

  val appLogic: ZIO[AppEnv, Throwable, Unit] = for {
    conf <- RIO.access[K8sConfigModule](_.get)
    coreV1Client <- RIO.access[ClientModule](cm => Core_v1Client.httpClient(cm.get, conf.currentContext.cluster.server))
    podList <- coreV1Client.listCoreV1NamespacedPod(nopTrace, "kube-system")
    _ <- podList.fold(pl => putStrLn(pl.asJson.deepDropNullValues.spaces2), putStrLn("Unauthorized"))
  } yield ()
}
