package routes

import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SwaggerAggregator {
  import endpoints.admin.AdminEndpoints.*
  import endpoints.global.GlobalEndpoints.*
  import endpoints.university.UniversityEndpoints.*
  import endpoints.student.StudentEndpoints.*

  def swaggerInterpreter[F[_]]() = SwaggerInterpreter().fromEndpoints[F](
    globalEndpoints ++ adminEndpoints ++ universityEndpoints ++ studentEndpoints,
    "Global endpoints",
    "1.0"
  )
}
