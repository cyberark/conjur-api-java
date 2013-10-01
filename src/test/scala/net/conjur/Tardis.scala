package net.conjur

import org.joda.time.{DateTimeUtils, DateTime}

/**
 * Time And Relative Dimension In Space
 */
trait Tardis {
  def timeTravel(to:DateTime)(stuffToDo:()=>Unit) : Unit = {
    DateTimeUtils.setCurrentMillisFixed(to.getMillis)
    try{ stuffToDo() }
    finally{ DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis()) }
  }

}
