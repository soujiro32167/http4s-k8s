package com.example

import com.example.K8sConfigModule.K8sConfigModule
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityDecoder, Uri}
import security.TLS
import zio._

//@accessible
object ClientModule {
  type ClientModule = Has[Client[Task]]

  trait Service extends org.http4s.client.Client[Task]

  def expect[T](uri: Uri)(implicit dec: EntityDecoder[Task, T]): ZIO[ClientModule, Throwable, T] = ZIO.accessM[ClientModule](_.get.expect(uri))

  val live: ZLayer[zio.ZEnv with K8sConfigModule, Throwable, ClientModule] = {
    import zio.interop.catz._

    ZIO.runtime[ZEnv].toManaged_.flatMap ( implicit rts => for {
      currentContext <- ZIO.access[K8sConfigModule](_.get.currentContext).toManaged_
      cm <-    BlazeClientBuilder[Task](rts.platform.executor.asEC)
        .withSslContextOption(TLS.establishSSLContext(currentContext))
        .resource.toManaged
    } yield cm
    ).toLayer
  }
}
