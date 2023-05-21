package ro.campuscompass.common.minio

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*
import io.minio.http.Method
import io.minio.*

import java.io.{ File, FileInputStream, InputStream }
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class MinIO[F[_]: Sync](client: MinioClient) {

  def createBucketIfNotExists(bucket: String): F[Unit] =
    for {
      exists <- Sync[F].blocking(
        client.bucketExists(
          BucketExistsArgs.builder()
            .bucket(bucket)
            .build()
        )
      )
      _ <-
        if !exists then
          Sync[F].blocking(
            client.makeBucket(
              MakeBucketArgs.builder().bucket(bucket).build()
            )
          )
        else
          Sync[F].unit
    } yield ()

  def downloadLink(bucket: String, key: String, expirationDays: Int = 7): F[String] = Sync[F].blocking(
    client.getPresignedObjectUrl(
      GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .`object`(key)
        .bucket(bucket)
        // This is the most horrific hack I've ever wrote out of laziness.
        // Shame one me!
        .expiry(expirationDays, TimeUnit.DAYS)
        .build()
    )
  )

  def uploadWithOverwrite(bucket: String, key: String, file: File): F[Unit] = for {
    _ <- Sync[F].blocking(client.removeObject(
      RemoveObjectArgs.builder()
        .bucket(bucket)
        .`object`(key)
        .build()
    )).handleError(_ => ())
    s <- Sync[F].delay(new FileInputStream(file))
    _ <- Sync[F].blocking(
      client.putObject(
        PutObjectArgs.builder()
          .bucket(bucket)
          .`object`(key)
          .stream(s, s.available(), -1)
          .build()
      )
    )
    _ <- Sync[F].delay(s.close())
  } yield ()
}

object MinIO {
  def apply[F[_]: Async](config: MinIOConfig): F[MinIO[F]] =
    Sync[F].delay(
      MinioClient.builder()
        .endpoint(config.endpoint)
        .credentials(config.accessKey, config.secretAccessKey)
        .build()
    ).map(new MinIO[F](_))

}
