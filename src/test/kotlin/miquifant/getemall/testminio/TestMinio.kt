/**
 * TODO
 *
 * Created by miquifant on 2021-02-07
 */
package miquifant.getemall.testminio

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.UploadObjectArgs
import io.minio.errors.MinioException
import kotlin.test.*


const val BUCKET = "getemall"

class TestMinio {
  @Test
  fun test1() {
    try {

      // Create a minioClient with the local MinIO server, its access key and secret key
      val minioClient: MinioClient = MinioClient.builder()
          .endpoint("http://localhost:9000")
          .credentials("minioadmin", "minioadmin")
          .build()

      // Make 'getemall' bucket if not exist
      if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
        // Make a new bucket called 'getemall'.
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build())
      }
      else println("Bucket 'getemall' already exists.")

      // Upload '/etc/hosts' as object name 'hosts.txt' to bucket 'getemall'
      minioClient.uploadObject (
          UploadObjectArgs.builder()
              .bucket(BUCKET)
              .`object`("hosts.txt")
              .filename("/etc/hosts")
              .build())

      println ("'/etc/hosts' is successfully uploaded as object 'hosts.txt' to bucket '$BUCKET'.")

    }
    catch (e: MinioException) {
      println("Error occurred: $e");
    }
  }
}
