package domain

import domain.Coordinates
import java.util.UUID

enum Entity:
  case Admin()
  case University(name:String,contactPerson:String,email:String,coordinates:Coordinates)
