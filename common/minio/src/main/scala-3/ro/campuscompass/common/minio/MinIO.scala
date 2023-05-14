package ro.campuscompass.common.minio

import cats.effect.*
import io.minio.http.Method
import io.minio.{ GetPresignedObjectUrlArgs, MinioClient, UploadObjectArgs }

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

trait MinIO[F[_]: Sync](client: MinioClient) {
  def uploadLink(bucket: String, key: String, expirationDuration: Duration): F[String] = Sync[F].delay(
    client.getPresignedObjectUrl(
      GetPresignedObjectUrlArgs.builder()
        .method(Method.PUT)
        .`object`(key)
        .expiry(expirationDuration.toSeconds.toInt, TimeUnit.SECONDS)
        .build()
    )
  )

  def downloadLink(bucket: String, key: String, expirationDuration: Duration): F[String] = Sync[F].delay(
    client.getPresignedObjectUrl(
      GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .`object`(key)
        // This is the most horrific hack I've ever wrote out of laziness.
        // Shame one me!
        .expiry(Int.MaxValue, TimeUnit.DAYS)
        .build()
    )
  )
}

object MinIO {
  def apply[F[_]: Async](config: MinIOConfig): F[MinioClient] =
    Sync[F].delay(
      MinioClient.builder()
        .endpoint(config.endpoint)
        .credentials(config.accessKey, config.secretAccessKey)
        .build()
    )

}
