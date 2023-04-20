package template

sealed trait Template {
  def recipient: String
  def subject: String
  def message: String
}
case class UniversityConfirmedTemplate(
    recipient: String,
    subject: String,
    message: String
) extends Template

case class ApplicationRegistered(
    recipient: String,
    subject: String,
    message: String
) extends Template
