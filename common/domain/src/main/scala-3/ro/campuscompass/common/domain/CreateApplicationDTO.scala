package ro.campuscompass.common.domain

import ro.campuscompass.common.domain.StudentData

import java.util.UUID

final case class CreateApplicationDTO(
  studentData: StudentData,
  studentId: UUID,
  programId: UUID,
)
