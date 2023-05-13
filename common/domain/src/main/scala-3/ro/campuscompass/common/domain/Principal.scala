package ro.campuscompass.common.domain

import java.util.UUID

enum Principal:
  case Admin
  case University(_id: UUID)
  case Student(_id: UUID)
  case Global(apiKey: String)
