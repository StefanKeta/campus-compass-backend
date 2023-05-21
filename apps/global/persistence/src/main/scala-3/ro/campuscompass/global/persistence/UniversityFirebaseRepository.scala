package ro.campuscompass.global.persistence

import cats.effect.kernel.Async
import cats.implicits.*
import com.google.cloud.firestore.Firestore
import ro.campuscompass.global.domain.UniversityFirebase

import java.util.concurrent.Executor

trait UniversityFirebaseRepository[F[_]] {
  def persistUniversity(universityFirebase: UniversityFirebase): F[Unit]
}

object UniversityFirebaseRepository {
  def apply[F[_]: Async](firestore: Firestore) = new UniversityFirebaseRepository[F]:
    val collection = firestore.collection("universities")
    override def persistUniversity(universityFirebase: UniversityFirebase): F[Unit] = {
      val uniFields = Map("name" -> universityFirebase.name)
      Async[F].delay(collection.document(universityFirebase.uniUserId.toString).set(uniFields).addListener(
        ()                  => (),
        (command: Runnable) => command.run()
      ))
    }

}
