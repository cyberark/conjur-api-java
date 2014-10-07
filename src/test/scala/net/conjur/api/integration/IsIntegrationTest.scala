package net.conjur.api.integration

import org.scalatest._

trait IntegrationTestsEnabled {
  lazy val integrationTestsEnabled = {
    val propertyValue = System.getProperty("net.conjur.integrationTestsEnabled")
    propertyValue != null && !"null".equals(propertyValue)
  }
}

trait IsIntegrationTest extends FlatSpec with IntegrationTestsEnabled {
  override protected def runTests(testName: Option[String], args: Args): Status = if(integrationTestsEnabled) {
    super.runTests(testName, args)
  }else{
    SucceededStatus
  }
}

trait IsIntegrationFeature extends FeatureSpec with IntegrationTestsEnabled {
  override def info : Informer = if(integrationTestsEnabled){
    super.info
  }else{
    new Informer {
      override def apply(message: String, payload: Option[Any]): Unit = {}
    }
  }

  override protected def feature(description: String)(fun: => Unit) {
    if(integrationTestsEnabled){
      super.feature(description)(fun)
    }
  }
}
