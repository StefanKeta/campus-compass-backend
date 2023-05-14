package ro.campuscompass.regional.httpserver.api.model

import ro.campuscompass.regional.domain.ApplicationStatus

import java.util.UUID

final case class UpdateApplicationStatusDTO(applicationId: UUID, applicationStatus: ApplicationStatus)
