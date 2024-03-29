package ro.campuscompass.common.domain

final case class Address(
  street: Option[String],
  number: Option[String],
  postalCode: Option[Int],
  town: Option[String],
  country: Option[String]
)
