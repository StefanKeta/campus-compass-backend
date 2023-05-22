package ro.campuscompass.common.firebase

import cats.Applicative
import cats.effect.kernel.{ Resource, Sync }
import cats.implicits.*
import com.google.auth.oauth2.{ AccessToken, GoogleCredentials }
import com.google.cloud.firestore.{ Firestore, FirestoreOptions }

import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.{ Calendar, Date }

object FirebaseClient {

  private def nextYear[F[_]: Sync] = Sync[F].delay {
    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.YEAR, 1)
    calendar.getTime
  }

  def initializeFirebaseDb[F[_]: Sync](firebaseConfig: FirebaseConfig): Resource[F, Firestore] = Resource.eval(nextYear.map {
    date =>
      val credentials = GoogleCredentials.create(new AccessToken(firebaseConfig.apiKey, date))
      val options = FirestoreOptions.getDefaultInstance.toBuilder
        .setProjectId(firebaseConfig.projectId)
        .setCredentials(credentials)
        .build()
      options.getService
  })

}
