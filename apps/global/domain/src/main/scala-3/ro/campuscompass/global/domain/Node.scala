package ro.campuscompass.global.domain

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class Node(coordinates: Coordinates, be: String, fe: String) derives ConfigReader
