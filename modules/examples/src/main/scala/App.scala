import ClientModule.ClientModule
import io.sk8s.client.core_v1.Core_v1Client
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.interop.catz._
import zio._
import zio.console._
import zio.console.Console



object App extends zio.App {
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    appLogic
        .tapError(t => putStrLn(t.toString))
      .provideCustomLayer(liveEnv).fold(_ => 1, _ => 0 )
  }

  val liveEnv: ZLayer[zio.ZEnv, Throwable, ClientModule] = ZIO.runtime[ZEnv].toManaged_.flatMap { implicit rts =>
    BlazeClientBuilder[Task](rts.platform.executor.asEC).resource.toManaged
  }.toLayer

  val nopTrace: String => Client[Task] => Client[Task] = _ => c => c

  val appLogic: ZIO[Console with ClientModule, Throwable, Unit] = for {
    coreV1Client <- RIO.access[ClientModule](c =>  Core_v1Client.httpClient(c.get, "https://kubernetes.docker.internal:6443"))
    podList <- coreV1Client.listCoreV1NamespacedPod(nopTrace, "kube-system")
    _ <- podList.fold(pl => putStrLn(pl.toString), putStrLn("Unauthorized"))
  } yield ()
}
