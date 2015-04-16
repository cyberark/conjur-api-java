package net.conjur.api.integration

import scala.collection.mutable
import net.conjur.api.{Endpoints, User, Credentials, Conjur}
import org.scalatest._
import net.conjur.api.authn.CachingAuthnProvider
import scala.util.Random
import org.apache.commons.logging.LogFactory

object RandomString {
  def randomString(n:Int):String = List.fill(n)(Random.nextPrintableChar()).mkString
  def randomString:String = randomString(12)
  def randomString(prefix:String):String = prefix + randomString
  def randomString(prefix:String, count:Int):String = prefix + randomString(count)
}

trait RandomStri

trait Env {
  def getEnv(name:String) = Option(System.getenv(name))
}

trait TestCredentials  extends Env {
  lazy val credentials = (getEnv("CONJUR_USERNAME"), getEnv("CONJUR_API_KEY")) match {
    case (Some(username), Some(password)) => new Credentials(username, password)
    case _ => throw new RuntimeException("You must set environment variables CONJUR_USERNAME and CONJUR_API_KEY")
  }
}

trait TestEndpoints extends Env {
  lazy val endpoints = getEnv("CONJUR_APPLIANCE_URL") match {
    case Some(url) => Endpoints getApplianceEndpoints url
    case _ => throw new RuntimeException("You must set the CONJUR_APPLIANCE_URL environemnt variable")
  }
}

object ClientWrapper extends TestEndpoints with TestCredentials {
  def apply(credentials: Credentials, endpoints: Endpoints) = new ClientWrapper(credentials,endpoints)
  def apply(credentials:Credentials) = new ClientWrapper(credentials, endpoints)
  def apply(endpoints:Endpoints) = new ClientWrapper(credentials, endpoints)
  def apply = new ClientWrapper(credentials, endpoints)
  def apply(username:String, password:String) = new ClientWrapper(new Credentials(username, password), endpoints)
  implicit def clientWrapperToConjur(clientWrapper: ClientWrapper):Conjur = clientWrapper.conjur
  implicit def clientWrapperToCredentials(clientWrapper:ClientWrapper):Credentials = clientWrapper.credentials
  implicit def userToClientWrapper(user:User):ClientWrapper = ClientWrapper(new Credentials(user.getLogin, user.getApiKey))
}


class ClientWrapper(val credentials: Credentials, val endpoints: Endpoints){
  lazy val conjur = new Conjur(credentials, endpoints)
}

trait ConjurFixtures extends SuiteMixin
    with TestCredentials
    with TestEndpoints
    with BeforeAndAfterAll { this: Suite =>
  import RandomString._
  import ClientWrapper._


  lazy val log = LogFactory getLog "net.api.conjur"
  override def beforeAll = {
    log.info("Conjur configured with endpoints " + Endpoints.getDefault)
   }

  private val users = mutable.Map.empty[String, ClientWrapper]

  private var _ns:String = null

  abstract override def withFixture(test:NoArgTest) = {
    _ns = admin.variables.createId
    super.withFixture(test)
  }

  def ns:String = _ns
  def ns(s:String):String = if(s.startsWith(ns)) s else ns + "-" + s
  def ns(s:Symbol):String = ns(s.toString)

  def admin : ClientWrapper = ClientWrapper(credentials, endpoints)

  def createUser(login:String):User = {
    val user = admin.users create ns(login)
    users += login -> user
    user
  }

  def createUser(login:String, password:String):User = {
    val user = admin.users.create(ns(login), password)
    users += login -> ClientWrapper(login, password)
    user
  }

  def loginAs(name:String):ClientWrapper = users.getOrElse(name, createUser(name))
  def loginAs(name:Symbol):ClientWrapper = loginAs(name.toString)


  def randomUsername = randomString("user-",8)
  def randomPassword = randomString(8)
}