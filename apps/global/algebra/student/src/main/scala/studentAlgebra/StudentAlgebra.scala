package studentAlgebra

import cats.effect.Async
import cats.effect.std.UUIDGen
import cats.implicits.*
import config.domain.{AdminConfiguration, EmailConfiguration}
import dao.{ApplicationDAO, UniversityDAO, UserDAO}
import domain.student.StudentApplication
import domain.user.Role
import emailAlgebra.EmailAlgebra
import error.StudentError.{AlreadyAppliedToUniversity, EmailAlreadyEnrolled, NonExistingUniversity}
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import org.typelevel.log4cats.Logger
import password.PasswordHasher
import template.{ApplicationRegistered, applicationRegisteredTemplate}

trait StudentAlgebra [F[_]]{
  def applyToUni(studentApplication: StudentApplication):F[Unit]
}

object StudentAlgebra{
  def apply[F[_]](mongoDatabase: MongoDatabase[F],emailAlgebra:EmailAlgebra[F],emailConfiguration: EmailConfiguration)(using F:Async[F], passwordHasher: PasswordHasher[F], uuidGen:UUIDGen[F], logger: Logger[F]) = new StudentAlgebra[F]:
    override def applyToUni(studentApplication: StudentApplication): F[Unit] = for{
      uniDocs <- mongoDatabase.getCollectionWithCodec[UniversityDAO]("universities")
      maybeUni <- uniDocs.find(Filter.eq("_id",studentApplication.appliedTo.toString)).first
      _ <- maybeUni match
        case Some(_) => attemptApplication(studentApplication)
        case None => logger.info(s"Cannot apply to non-existing university") *> F.raiseError(NonExistingUniversity(s"Student ${studentApplication.email} tried to apply to a non-existent university"))
    }yield ()

    private def generateApplication(application: StudentApplication) = for{
      appId <- uuidGen.randomUUID
    } yield ApplicationDAO(appId,application.appliedTo,application.firstName,application.lastName,application.email)

    private def attemptApplication(application: StudentApplication) = for{
      docs <- mongoDatabase.getCollectionWithCodec[ApplicationDAO]("applications")
      existingApplication <- docs.find(Filter.eq("universityId", application.appliedTo.toString).and(Filter.eq("email", application.email))).first
      _ <- existingApplication match
        case Some(_) => logger.warn(s"Cannot apply to uni -> student already applied") *> F.raiseError(AlreadyAppliedToUniversity(s"The student ${application.email} already applied to ${application.appliedTo}"))
        case None => for {
          password <- passwordHasher.generateRawPassword()
          _ <- insertUser(application, password)
          applicationDao <- generateApplication(application)
          _ <- docs.insertOne(applicationDao)
          name = s"${application.firstName.capitalize} ${application.lastName.capitalize}"
          _ <- emailAlgebra.sendEmail(ApplicationRegistered(application.email, "University Application Confirmation", applicationRegisteredTemplate(name, application.email, password, emailConfiguration.sender)))
          _ <- logger.info(s"Student ${application.email} applied to university ${application.appliedTo} successfully")
        } yield ()
    } yield ()

    private def insertUser(application: StudentApplication,password:String) = for {
      docs <- mongoDatabase.getCollectionWithCodec[UserDAO]("users")
      maybeUser <- docs.find(Filter.eq("username", application.email)).first
      _ <- maybeUser match
        case Some(_) => logger.info(s"User already enrolled") *> F.raiseError(EmailAlreadyEnrolled(s"User already enrolled, make him log into the platform and apply there"))
        case None => for {
          id <- uuidGen.randomUUID
          hashedPassword <- passwordHasher.encryptPassword(password)
          user = UserDAO(id, application.email, hashedPassword, Role.Student)
          _ <- docs.insertOne(user)
        } yield ()
    } yield ()
}
