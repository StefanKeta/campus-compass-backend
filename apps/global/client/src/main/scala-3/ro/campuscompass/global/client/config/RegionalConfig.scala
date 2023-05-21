package ro.campuscompass.global.client.config

import ro.campuscompass.global.domain.Node

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class RegionalConfig(nodes: List[Node]) derives ConfigReader
