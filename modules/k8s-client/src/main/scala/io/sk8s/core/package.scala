import java.time.Instant

import io.circe.Decoder
import io.circe.parser._

// todo: effects!
package object core {
  // Certificates and keys can be specified in configuration either as paths to files or embedded PEM data
  type PathOrData = Either[String, Array[Byte]]

  sealed trait AuthInfo

  sealed trait AccessTokenAuth extends AuthInfo {
    def accessToken: String
  }

  object NoAuth extends AuthInfo {
    override def toString: String = "NoAuth"
  }

  final case class BasicAuth(userName: String, password: String) extends AuthInfo {
    override def toString: String = s"${getClass.getSimpleName}(userName=$userName,password=<redacted>)"
  }

  final case class TokenAuth(token: String) extends AccessTokenAuth {

    override def accessToken: String = token

    override def toString: String = s"${getClass.getSimpleName}(token=<redacted>)"
  }

  final case class CertAuth(clientCertificate: PathOrData, clientKey: PathOrData, user: Option[String]) extends AuthInfo {
    override def toString: String = new StringBuilder()
      .append(getClass.getSimpleName)
      .append("(")
      .append {
        clientCertificate match {
          case Left(certPath: String) => "clientCertificate=" + certPath + " "
          case Right(_) => "clientCertificate=<PEM masked> "
        }
      }
      .append {
        clientKey match {
          case Left(certPath: String) => "clientKey=" + certPath + " "
          case Right(_) => "clientKey=<PEM masked> "
        }
      }
      .append("userName=")
      .append(user.getOrElse(""))
      .append(" )")
      .mkString
  }

  sealed trait AuthProviderAuth extends AccessTokenAuth {
    def name: String
  }

  // 'jwt' supports an oidc id token per https://kubernetes.io/docs/admin/authentication/#option-1---oidc-authenticator
  // - but does not yet support token refresh
  final case class OidcAuth(idToken: String) extends AuthProviderAuth {
    override val name = "oidc"

    override def accessToken: String = idToken

    override def toString = """OidcAuth(idToken=<redacted>)"""
  }

  final case class GcpAuth private(private val config: GcpConfiguration) extends AuthProviderAuth {
    override val name = "gcp"

    @volatile private var refresh: Option[GcpRefresh] = config.cachedAccessToken.map(token => GcpRefresh(token.accessToken, token.expiry))

    private def refreshGcpToken(): GcpRefresh = {
      val output = config.cmd.execute()
      val parsed = decode[GcpRefresh](output)
      refresh = parsed.toOption
      parsed.toOption.get
    }

    def accessToken: String = this.synchronized {
      refresh match {
        case Some(expired) if expired.expired =>
          refreshGcpToken().accessToken
        case None =>
          refreshGcpToken().accessToken
        case Some(token) =>
          token.accessToken
      }
    }

    override def toString =
      """GcpAuth(accessToken=<redacted>)""".stripMargin
  }

  final private[core] case class GcpRefresh(accessToken: String, expiry: Instant) {
    def expired: Boolean = Instant.now.isAfter(expiry.minusSeconds(20))
  }

  private[core] object GcpRefresh {
    // todo - the path to read this from is part of the configuration, use that instead of
    // hard coding.
//    implicit val gcpRefreshReads: Reads[GcpRefresh] = (
//      (JsPath \ "credential" \ "access_token").read[String] and
//        (JsPath \ "credential" \ "token_expiry").read[Instant]
//      ) (GcpRefresh.apply _)
    implicit val dec: Decoder[GcpRefresh] = Decoder.instance( cursor => for {
        token <- cursor.downField("credential").get[String]("access_token")
        expiry <- cursor.downField("credential").get[Instant]("token_expiry")
      } yield GcpRefresh(token, expiry)
    )
  }

  final case class GcpConfiguration(cachedAccessToken: Option[GcpCachedAccessToken], cmd: GcpCommand)

  final case class GcpCachedAccessToken(accessToken: String, expiry: Instant) {
    def expired: Boolean = Instant.now.isAfter(expiry.minusSeconds(20))
  }

  final case class GcpCommand(cmd: String, args: String) {

    import scala.sys.process._

    def execute(): String = s"$cmd $args".!!
  }

  object GcpAuth {
    def apply(accessToken: Option[String], expiry: Option[Instant], cmdPath: String, cmdArgs: String): GcpAuth = {
      val cachedAccessToken = for {
        token <- accessToken
        exp <- expiry
      } yield GcpCachedAccessToken(token, exp)
      new GcpAuth(
        GcpConfiguration(
          cachedAccessToken = cachedAccessToken,
          GcpCommand(cmdPath, cmdArgs)
        )
      )
    }
  }
}
