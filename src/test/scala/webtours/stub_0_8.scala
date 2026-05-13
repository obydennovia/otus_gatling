package webtours

import io.gatling.core.Predef.{incrementUsersPerSec, _}
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class stub_0_8 extends Simulation {
  val maxDuration = 3720  // Длительность теста в секундах
  setUp(
    Scenarious.CommonScenario() // Что делает пользователь
      .inject(
        //atOnceUsers(1)
        incrementUsersPerSec(0.0)       // Добавить по n пользователей/сек. на каждой ступени
          .times(1)                     // Количество ступеней увеличения
          .eachLevelLasting(60.minutes) // Длительность ступени
          //.separatedByRampsLasting(10.seconds)  // Длительность плавного перехода (разгона) между ступенями
          .startingFrom(3)              // Начальное количество пользователей в секунду
      )
  ).//protocols(webtours.webTours.proxy(Proxy("localhost", 8888)))
    protocols(webtours.webTours)
    .maxDuration(maxDuration)

}
