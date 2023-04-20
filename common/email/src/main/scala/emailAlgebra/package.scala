import cats.effect.{Fiber, Outcome}

package object emailAlgebra {
  type EmailOutcome[F[_]] = Outcome[F, Throwable, Unit]
  type EmailFiber[F[_]] = Fiber[F, Throwable, Unit]
}
