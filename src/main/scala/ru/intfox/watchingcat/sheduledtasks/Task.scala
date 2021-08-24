package ru.intfox.watchingcat.sheduledtasks

import cats.effect.IO

import scala.concurrent.duration.FiniteDuration

trait Task {
  def run(): IO[Unit]
  def sleepTime(): FiniteDuration
}