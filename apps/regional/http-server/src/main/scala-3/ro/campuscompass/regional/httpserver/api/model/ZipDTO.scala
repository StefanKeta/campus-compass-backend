package ro.campuscompass.regional.httpserver.api.model

import java.io.File
import sttp.model.Part

final case class ZipDTO(zip: Part[File])
