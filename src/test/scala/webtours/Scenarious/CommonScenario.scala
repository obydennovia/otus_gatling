package webtours.Scenarious

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import webtours.Actions

object CommonScenario {
  def apply(): ScenarioBuilder = new CommonScenario().scn
}

class CommonScenario {

  // Объявляем фидер (укажите путь к вашему файлу)
  val userFeeder = csv("data/users.csv").circular // гоняем пользователей по кругу

  val scn = scenario("WebTours")
    .feed(userFeeder) // Подключаем данные из CSV в текущую сессию
    .exec(Actions.clearCookiesAction)
    .exec(Actions.rootPageTransaction)
    //.pause(2)
    .exec(Actions.loginTransaction) // Логин/пароль из файла должны быть здесь
    //.pause(2)
    .exec(Actions.flightTransaction)
    //.pause(2)
    .exec(Actions.oneWayTicketTransaction)
    //.pause(2)
    .exec(Actions.rootPageTransaction)
    //.pause(2)
}