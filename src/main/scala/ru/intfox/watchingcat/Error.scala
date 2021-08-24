package ru.intfox.watchingcat

import org.http4s.Status

object Error {
  trait HttpError {
    def status: Status
  }
  class NotFound() extends Throwable with HttpError {
    def status = Status.NotFound
  }
}
