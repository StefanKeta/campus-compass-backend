package ro.campuscompass.regional.persistance

import cats.effect.Async
import com.google.cloud.firestore.Firestore
import ro.campuscompass.regional.domain.HousingCredentials

trait HousingCredentialsFirebaseRepository[F[_]] {
  def insert(creds: HousingCredentials): F[Unit]
}

object HousingCredentialsFirebaseRepository {
  def apply[F[_]: Async](firestore: Firestore) = new HousingCredentialsFirebaseRepository[F]:
    val collection = firestore.collection("users")

    override def insert(creds: HousingCredentials): F[Unit] = {
      Async[F].delay(collection.document(s"${creds.studentId}").set(java.util.Map.of(
        "userType",
        "STUDENT",
        "universityId",
        s"${creds.universityId}",
        "username",
        s"${creds.credentials.username}",
        "password",
        s"${creds.credentials.password}"
      )).addListener(
        ()                  => (),
        (command: Runnable) => command.run()
      ))
    }
}
