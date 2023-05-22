package ro.campuscompass.global.domain

import ro.campuscompass.common.crypto.JWT

final case class LoginResponseDTO(jwt: JWT, redirectTo: Option[String])
