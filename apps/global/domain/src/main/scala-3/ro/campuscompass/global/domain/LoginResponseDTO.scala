package ro.campuscompass.global.domain

import ro.campuscompass.common.crypto.JWT

sealed trait LoginResponseDTO(jwt: JWT)

final case class GeneralLoginResponse(jwt: JWT) extends LoginResponseDTO(jwt)
final case class UniversityLoginResponse(jwt: JWT, redirectTo: String) extends LoginResponseDTO(jwt)
