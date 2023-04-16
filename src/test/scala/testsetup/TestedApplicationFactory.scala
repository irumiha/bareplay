package testsetup

import application.ProdLoader
import org.scalatestplus.play.FakeApplicationFactory
import play.api.{Application, ApplicationLoader, Environment}

trait TestedApplicationFactory extends FakeApplicationFactory:

  def applicationContext: ApplicationLoader.Context =
    val env = Environment.simple()
    ApplicationLoader.Context.create(env)

  def build(): Application =
    val loader = new ProdLoader()
    loader.load(applicationContext)

  def fakeApplication(): Application = build()
