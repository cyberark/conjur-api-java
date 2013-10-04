package net.conjur.api.integration

import scala.collection.mutable
import net.conjur.api.{User, Credentials, Conjur}
import org.scalatest._
import net.conjur.api.authn.CachingAuthnProvider
import scala.util.Random

object RandomString {
  private lazy val pattern = "^(.*?)\\$(\\d+)\\$(.*)$".r
  def randomString(n:Int):String = List.fill(n)(Random.nextPrintableChar()).mkString
  def randomString:String = randomString(12)
  def randomString(prefix:String):String = prefix + randomString
  def randomString(prefix:String, count:Int):String = prefix + randomString(count)
}

trait RandomString

object ClientWrapper {
  private val clients = mutable.Map.empty[Credentials, Conjur]
  def client(auth:Credentials) = clients.getOrElseUpdate(auth, new Conjur(new CachingAuthnProvider(auth)))

  implicit def ClientWrapperToClient(cv:ClientWrapper) = cv.client
  implicit def ClientWrapperToCredentials(cv:ClientWrapper) = cv.credentials
  implicit def UserToClientWrapper(user:User) = ClientWrapper(user.getLogin, user.getApiKey)
  implicit def CredentialsToClientWrapper(creds:Credentials) = ClientWrapper(creds)

  def apply(creds:Credentials):ClientWrapper = new ClientWrapper(creds)
  def apply(login:String, pswd:String):ClientWrapper = apply(new Credentials(login,pswd))

}
class ClientWrapper(val credentials:Credentials) {
  def client = ClientWrapper.client(credentials)
  def login  = credentials.getUsername
  def withPassword(pw:String) = ClientWrapper(login, pw)
}

object TestCredentials {
  private lazy val pattern = "([^:]+):(.+)".r
  lazy val credentials = {
    System.getProperty(Credentials.CREDENTIALS_PROPERTY) match {
      case null => throw new Exception("no credentials set in system property" + Credentials.CREDENTIALS_PROPERTY)
      case pattern(login, password) => new Credentials(login, password)
      case s => throw new Exception("credentials must be like 'username:password' (got '" + s + "'")
    }
  }
}

trait TestCredentials {
  def credentials = TestCredentials.credentials
}


trait ConjurFixtures extends SuiteMixin
    with TestCredentials { this: Suite =>
  import RandomString._

  private val users = mutable.Map.empty[String, ClientWrapper]

  private var _ns:String = null

  abstract override def withFixture(test:NoArgTest) = {
    _ns = admin.variables.createId
    super.withFixture(test)
  }

  def ns:String = _ns
  def ns(s:String):String = if(s.startsWith(ns)) s else ns + "-" + s
  def ns(s:Symbol):String = ns(s.toString)

  def admin : ClientWrapper = credentials

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