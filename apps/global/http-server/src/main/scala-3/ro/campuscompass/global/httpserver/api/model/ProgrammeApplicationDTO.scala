package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.ProgrammeApplication

import java.util.UUID

final case class ProgrammeApplicationDTO(
  programmeId: UUID,
  universityUserId: UUID
) {
  def domain(studentId: UUID, userId: UUID) = ProgrammeApplication(
    studentId        = studentId,
    programmeId      = programmeId,
    universityUserId = universityUserId
  )
}
