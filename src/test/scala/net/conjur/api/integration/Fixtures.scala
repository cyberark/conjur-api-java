package net.conjur.api.integration

import scala.collection.mutable
import net.conjur.api.{User, Credentials, Conjur}
import org.scalatest._
import net.conjur.api.authn.CachingAuthnProvider
import scala.util.Random

trait RandomStringLike {
  def ++ = next
  def next:String
}

object RandomString extends RandomStringLike {
  private class Impl(prefix:String, length:Int) extends RandomStringLike {
    def next = prefix + List.fill(length)(Random.nextPrintableChar()).mkString
  }

  private lazy val impl = new Impl("", 6)

  def apply(prefix:String, length:Int):RandomStringLike = new Impl(prefix, length)
  def apply(prefix:String):RandomStringLike = new Impl(prefix, 16)
  def apply(length:Int):RandomStringLike = new Impl("", length)
  def apply():RandomStringLike = new Impl("", 6)

  def next = impl.next

}

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


trait ConjurFixtures extends SuiteMixin { this: Suite =>

  private val users = mutable.Map.empty[String, ClientWrapper]

  private var _ns:String = null

  abstract override def withFixture(test:NoArgTest) = {
    _ns = admin.variables.createId
    super.withFixture(test)
  }

  def ns:String = _ns
  def ns(s:String):String = if(s.startsWith(ns)) s else ns + "-" + s
  def ns(s:Symbol):String = ns(s.toString)

  def admin : ClientWrapper = Credentials.fromSystemProperties

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


  def randomUsername = RandomString("login-", 6) ++
  def randomPassword = RandomString() ++
}