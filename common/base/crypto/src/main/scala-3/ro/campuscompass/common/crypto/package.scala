package ro.campuscompass.common.crypto

import ro.campuscompass.common.`type`.OpaqueTypeCompanion

opaque type JWT = String

object JWT extends OpaqueTypeCompanion[JWT, String]
