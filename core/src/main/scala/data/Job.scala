// package es.uvigo.ei.sing.funpep
// package data

// import java.nio.file.Path
// import java.util.UUID

// import scalaz._
// import scalaz.Scalaz._
// import scalaz.effect.IO

// import argonaut._
// import argonaut.Argonaut._

// import util.IOUtils._
// import util.JsonUtils._


// final case class Job (
  // id:          UUID,
  // status:      Job.Status,
  // email:       Email,
  // threshold:   Double,
  // annotations: Map[FastaEntry.ID, String]
// ) {

  // // aliases of JobPrinter.to*
  // def toJsonString: String = JobPrinter.toJsonString(this)
  // def toJsonFile(file: Path): IOThrowable[Unit] = JobPrinter.toJsonFile(file)(this)

// }

// object Job {

  // sealed trait Status
  // case object Initial  extends Status
  // case object Created  extends Status
  // case object Queued   extends Status
  // case object Started  extends Status
  // case object Finished extends Status
  // case object Failed   extends Status

  // object Status {

    // def apply(str: String): Option[Status] =
      // str.toLowerCase.trim match {
        // case "initial"  ⇒ Initial.some
        // case "created"  ⇒ Created.some
        // case "queued"   ⇒ Queued.some
        // case "started"  ⇒ Started.some
        // case "finished" ⇒ Finished.some
        // case "failed"   ⇒ Failed.some
        // case _          ⇒ none[Status]
      // }

    // implicit val StatusEqual: Equal[Status] = Equal.equalA
    // implicit val StatusShow:  Show[Status]  = Show.showA

    // implicit val StatusDecodeJson: DecodeJson[Status] =
      // optionDecoder(_.string >>= (Status.apply), "Status")

    // implicit val StatusEncodeJson: EncodeJson[Status] =
      // StringEncodeJson.contramap(_.shows)

  // }

  // @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Nothing", "org.brianmckenna.wartremover.warts.Any"))
  // implicit val JobCodecJson: CodecJson[Job] =
    // casecodec5(Job.apply, Job.unapply)("id", "status", "email", "threshold", "annotations")

  // // aliases of JobParser.from*
  // def apply(str:  String): Throwable ∨ Job  = JobParser.fromJsonString(str)
  // def apply(file: Path  ): IOThrowable[Job] = JobParser.fromJsonFile(file)

// }

// object JobParser {

  // def fromJsonString(str: String): Throwable ∨ Job =
    // str.decodeEither[Job] leftMap { err ⇒ new IllegalArgumentException(err) }

  // def fromJsonFile(file: Path): IOThrowable[Job] =
    // file.contentsAsString >>= { str ⇒ fromJsonString(str).point[IO] }

// }

// object JobPrinter {

  // import \/.{ fromTryCatchNonFatal ⇒ tryCatch }

  // def toJsonString(s: ⇒ Job): String =
    // implicitly[EncodeJson[Job]].encode(s).spaces2

  // def toJsonFile(file: Path)(s: ⇒ Job): IOThrowable[Unit] =
    // file.openIOWriter.bracket(_.closeIO) {
      // writer ⇒ tryCatch(writer.write(toJsonString(s))).point[IO]
    // }

// }
