package webtours

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt


class Debug extends Simulation {

  val maxDuration = 2100  // Длительность теста в секундах
  setUp(
    Scenarious.CommonScenario() // Что делает пользователь
      .inject(
          //atOnceUsers(1)
          incrementUsersPerSec(1.0)       // Добавить по n пользователей/сек. на каждой ступени
            // incrementUsersPerSec(10.0)       // Добавить по n пользователей/сек. на каждой ступени
            .times(4)                     // Количество ступеней увеличения
            .eachLevelLasting(5.minutes) // Длительность ступени
            .separatedByRampsLasting(10.seconds)  // Длительность плавного перехода (разгона) между ступенями
            .startingFrom(1)              // Начальное количество пользователей в секунду
      )
  )//.protocols(webtours.webTours.proxy(Proxy("localhost", 8888)))
    .protocols(webtours.webTours.proxy(Proxy("localhost", 8888)))
    .maxDuration(maxDuration)


//  setUp(
//    Scenarious.CommonScenario() // что делает пользователь
//      .inject(atOnceUsers(1)) // как и сколько пользователей запускаем
//  ).protocols(webtours.webTours.proxy(Proxy("localhost", 8888))) // куда и с какими HTTP-настройками бьём

}
