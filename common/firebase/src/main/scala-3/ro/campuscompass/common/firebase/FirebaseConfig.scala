package ro.campuscompass.common.firebase
import pureconfig.*
import pureconfig.generic.derivation.default.*
final case class FirebaseConfig(apiKey: String,projectId:String) derives ConfigReader
