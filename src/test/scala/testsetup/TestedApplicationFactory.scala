package testsetup

import application.AppLoader
import org.scalatestplus.play.FakeApplicationFactory
import play.api.{Application, ApplicationLoader, Environment}

trait TestedApplicationFactory extends FakeApplicationFactory {

  private class GreetingApplicationBuilder {
    def build(): Application = {
      val env = Environment.simple()
      val context = ApplicationLoader.Context.create(env)
      val loader = new AppLoader()
      loader.load(context)
    }
  }

  def fakeApplication(): Application = new GreetingApplicationBuilder().build()

}
