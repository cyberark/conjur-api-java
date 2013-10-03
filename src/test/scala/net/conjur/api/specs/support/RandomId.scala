package net.conjur.api.specs.support

import scala.util.Random

class RandomId(val prefix:String, val length:Int)  {
  private lazy val rand = new Random
  def this() = this("", 10)
  def ++ = prefix + rand.nextString(length)
}
