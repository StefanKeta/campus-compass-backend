package domain.user

import domain.Coordinates

enum Entity:
  case Admin()
  case University(name:String,contactPerson:String,email:String,coordinates:Coordinates)
  case Student(firstName:String,lastName:String,email:String)
