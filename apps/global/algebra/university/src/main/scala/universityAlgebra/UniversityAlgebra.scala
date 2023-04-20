package universityAlgebra

import cats.effect.Async
import cats.effect.std.UUIDGen
import cats.implicits.*
import dao.UniversityDAO
import domain.university.EnrollInput
import mongo4cats.database.MongoDatabase
import org.typelevel.log4cats.Logger

import java.util.UUID

trait UniversityAlgebra [F[_]]{
  def enrollUniversity(enrollInput: EnrollInput):F[Unit]
}

object UniversityAlgebra{
  def apply[F[_]](mongoDatabase: MongoDatabase[F])(using F:Async[F], uuidGen: UUIDGen[F], logger: Logger[F]) = new UniversityAlgebra[F]:
    override def enrollUniversity(enrollInput: EnrollInput): F[Unit] = for{
      docs <- mongoDatabase.getCollectionWithCodec[UniversityDAO]("universities")
      id <- uuidGen.randomUUID
      userId <- uuidGen.randomUUID
      university = generateUniversityDocument(id,userId,enrollInput)
      _ <- docs.insertOne(university)
      _ <- logger.info(s"University ${university.name} enrolled into the system!")
    } yield ()

    private def generateUniversityDocument(id:UUID,userId:UUID,enrollInput: EnrollInput) = UniversityDAO(_id = id, userId = userId, name = enrollInput.name, contactPerson = enrollInput.contactPerson, email = enrollInput.email, coordinates = enrollInput.coordinates)
}
