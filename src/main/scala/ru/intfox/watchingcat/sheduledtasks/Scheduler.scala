package ru.intfox.watchingcat.sheduledtasks

import cats.effect.{Deferred, IO, ParallelF, Resource}
import cats.implicits._

object Scheduler {
  def apply(tasks: List[Task]): IO[Unit] =
    tasks.map(task => fs2.Stream.repeatEval(task.run().attempt).metered(task.sleepTime())).reduce(_.merge(_)).compile.drain
}
