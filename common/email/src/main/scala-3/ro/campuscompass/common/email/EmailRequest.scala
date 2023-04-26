package ro.campuscompass.common.email

final case class EmailRequest(
  from: EmailAddress,
  to: List[EmailAddress],
  subject: Subject,
  content: Content
)
