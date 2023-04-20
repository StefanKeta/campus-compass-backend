package adminAlgebra
import adminAlgebra.*
import cats.effect.kernel.{Outcome, Sync}
import cats.effect.std.Random
import cats.implicits.*
import cats.syntax.*
import config.domain.EmailConfiguration
import password.PasswordHasher
import dao.{ConfirmedUniversityDAO, UniversityDAO, UserDAO}
import domain.user.Entity.*
import domain.user.Role
import emailAlgebra.EmailAlgebra
import error.AdminError
import error.AdminError.*
import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.database.MongoDatabase
import mongo4cats.circe.*
import mongo4cats.operations.Filter
import org.typelevel.log4cats.Logger
import template.*

import java.util.UUID
trait AdminAlgebra[F[_]]{
  def getUniversities():F[List[UniversityDAO]]
  def confirmExistence(admin: Admin,universityId:UUID):F[Unit]
}

object AdminAlgebra{
  def apply[F[_]](mongoDatabase: MongoDatabase[F], emailAlgebra: EmailAlgebra[F], emailConfiguration: EmailConfiguration)(using F:Sync[F], logger:Logger[F], passwordHasher: PasswordHasher[F],random: Random[F]): AdminAlgebra[F] = new AdminAlgebra[F]:
    override def getUniversities(): F[List[UniversityDAO]] = for{
      _ <- logger.info("Getting all the universities by admin")
      docs <- mongoDatabase.getCollectionWithCodec[UniversityDAO]("universities")
      universities <- docs.find.all
      _ <- logger.info(s"Got ${universities.size} universities from database")
    } yield universities.toList

    override def confirmExistence(admin: Admin, universityId:UUID): F[Unit] = for{
      _ <- logger.info(s"Confirming university with $universityId...")
      docs <- mongoDatabase.getCollectionWithCodec[UniversityDAO]("universities")
      maybeUniversity <- docs.find(Filter.eq("_id",universityId.toString)).first
      _ <- maybeUniversity match
        case Some(uni) => insertUniversityToUsers(uni)
        case None => logger.error(s"University with id: ${universityId} does not exist!") *> F.raiseError(UniversityNotFound(s"University with id: $universityId does not exist!"))
      _ <- logger.info(s"University $universityId confirmed!")
    } yield ()

    private def insertUniversityToUsers(universityDAO: UniversityDAO): F[Unit] = for{
      credentials <- generateCredentials()
      hashedPassword <- passwordHasher.encryptPassword(credentials.password)
      _ <- insertConfirmation(universityDAO.userId, credentials.copy(password = hashedPassword))
      _ <- emailAlgebra.sendEmail(UniversityConfirmedTemplate(universityDAO.email, "University confirmation", confirmUniversityHTML(credentials.username, credentials.password, emailConfiguration.sender)))
    } yield ()

    private def generateCredentials():F[Credentials] = for {
      username <- random.betweenInt(10_000_000,999_999_999)
      password <- passwordHasher.generateRawPassword()
    } yield Credentials(username.toString,password)

    private def insertConfirmation(universityId:UUID,credentials: Credentials) = for{
      docs <- mongoDatabase.getCollectionWithCodec[UserDAO]("users")
      res <- docs.find(Filter.eq("_id",universityId)).first
      _ <- res match
        case None => docs.insertOne(UserDAO(universityId,credentials.username,credentials.password,Role.University))
        case _ => logger.error("You cannot confirm the identity twice!") *> F.raiseError(UniversityAlreadyConfirmed("The university you are trying to get confirmation for already exists!"))
    } yield ()

    private case class Credentials(username:String,password:String)
}
