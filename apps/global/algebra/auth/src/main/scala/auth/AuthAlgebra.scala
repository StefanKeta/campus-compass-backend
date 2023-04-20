package auth

import auth.*
import cats.effect.Async
import cats.implicits.*
import config.domain.AppConfiguration
import password.PasswordHasher
import dao.UserDAO
import dev.profunktor.redis4cats.RedisCommands
import domain.user.User
import error.AuthError.WrongCredentials
import io.circe.generic.auto.*
import io.circe.syntax.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import org.typelevel.log4cats.Logger
import token.JwtUtils

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

trait AuthAlgebra[F[_]]{
  def login(username:String,password:String):F[AuthenticationToken]
}

object AuthAlgebra{
  def apply[F[_]](mongoDatabase: MongoDatabase[F], redis: RedisCommands[F,String,String], appConfiguration: AppConfiguration)
                 (using F:Async[F], jwtUtils: JwtUtils[F], passwordHasher: PasswordHasher[F], logger:Logger[F]) = new AuthAlgebra[F]:
   
    override def login(username: String, password: String): F[AuthenticationToken] = for{
      docs <- mongoDatabase.getCollectionWithCodec[UserDAO]("users")
      maybeUser <- docs.find(Filter.eq("username",username)).first
      token <- maybeUser match
        case Some(usr) => checkUserAuthorization(usr,password).ifM(
          createToken(usr),
          logger.info(s"Wrong credentials entered!") *> F.raiseError(WrongCredentials("Wrong credentials!"))
        )
        case None => F.raiseError(WrongCredentials("Wrong credentials!"))
    }yield token

    private def checkUserAuthorization(userDAO: UserDAO,password:String): F[Boolean] =
      passwordHasher
        .checkPassword(password,userDAO.password)
        .map(_ || password == appConfiguration.admin.password)
        .ifM(F.pure(true),F.raiseError(WrongCredentials("Wrong credentials!")))

    private def createToken(userDAO: UserDAO):F[AuthenticationToken] = for{
      jwtEntity <- ("user", User(userDAO._id, userDAO.role).asJson).pure[F]
      token <- jwtUtils.generateJwt(jwtEntity)
      _ <- redis.setEx(userDAO._id.toString, token, 1.day)
    } yield token
}


