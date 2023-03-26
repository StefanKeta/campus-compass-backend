package adminAlgebra
import adminAlgebra.*
import cats.effect.kernel.Sync
import cats.implicits.*
import dao.UniversityDAO
import domain.Coordinates
import domain.Entity.*
import io.circe.generic.auto.*
import mongo4cats.bson.ObjectId
import mongo4cats.circe.*
import mongo4cats.database.MongoDatabase
import mongo4cats.circe.*
import org.typelevel.log4cats.Logger
trait AdminAlgebra[F[_]]{
  def getUniversities():F[List[UniversityDAO]]
}

object AdminAlgebra{
  def apply[F[_]](mongoDatabase: MongoDatabase[F])(using F:Sync[F],logger:Logger[F]): AdminAlgebra[F] = new AdminAlgebra[F]:
    override def getUniversities(): F[List[UniversityDAO]] = for{
      _ <- logger.info("Getting all the universities by admin")
      docs <- mongoDatabase.getCollectionWithCodec[UniversityDAO]("universities")
      universities <- docs.find.all
      _ <- logger.info(s"Got ${universities.size} universities from database")
    } yield universities.toList
}
