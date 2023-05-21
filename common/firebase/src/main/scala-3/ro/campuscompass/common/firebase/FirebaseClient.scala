package ro.campuscompass.common.firebase

import cats.effect.kernel.{Resource, Sync}
import cats.implicits.*
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{Firestore, FirestoreOptions}

object FirebaseClient {
  private val credentials = GoogleCredentials.getApplicationDefault()

  def initializeFirebaseDb[F[_]: Sync](firebaseConfig: FirebaseConfig): Resource[F, Firestore] = {
    val options = FirestoreOptions.getDefaultInstance.toBuilder
      .setProjectId(firebaseConfig.projectId)
      .setCredentials(credentials)
      .build()
    Resource.eval(Sync[F].delay(options.getService))
  }

}
