package dao

import domain.Coordinates
import mongo4cats.bson.ObjectId
import domain.Entity.University

case class UniversityDAO(_id:ObjectId,name: String, contactPerson: String, email: String, coordinates: Coordinates)
