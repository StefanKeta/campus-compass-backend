package ro.campuscompass.global.client.api.model.response

import ro.campuscompass.common.crypto.JWT

final case class ViewApplicationRedirectDTO(jwt: JWT, redirectTo: String)
