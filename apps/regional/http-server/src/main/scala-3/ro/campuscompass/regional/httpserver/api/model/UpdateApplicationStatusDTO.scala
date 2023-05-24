package ro.campuscompass.regional.httpserver.api.model

import java.util.UUID

final case class UpdateApplicationStatusDTO(applicationId: UUID, applicationStatus: String)
