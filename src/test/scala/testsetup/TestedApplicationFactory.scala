package testsetup

import application.Loader
import org.scalatestplus.play.FakeApplicationFactory
import play.api.{Application, ApplicationLoader, Environment}

trait TestedApplicationFactory extends FakeApplicationFactory {

  private class GreetingApplicationBuilder {
    def build(): Application = {
      val env = Environment.simple()
      val context = ApplicationLoader.Context.create(env)
      val loader = new Loader()
      loader.load(context)
    }
  }

  def fakeApplication(): Application = new GreetingApplicationBuilder().build()

}
