package ro.campuscompass.global.domain

final case class Address(
  street: String,
  number: String,
  postalCode: Int,
  town: String,
  country: String
)
