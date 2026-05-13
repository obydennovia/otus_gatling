package webtours

import io.gatling.core.Predef.{incrementUsersPerSec, _}
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class fMaxOpened extends Simulation {

  val maxDuration = 2100  // Длительность теста в секундах
  setUp(
    Scenarious.CommonScenario() // Что делает пользователь
      .inject(
        //atOnceUsers(1)
        incrementUsersPerSec(1.0)       // Добавить по n пользователей/сек. на каждой ступени
        // incrementUsersPerSec(10.0)       // Добавить по n пользователей/сек. на каждой ступени
          .times(3)                     // Количество ступеней увеличения
          .eachLevelLasting(7.minutes) // Длительность ступени
          .separatedByRampsLasting(10.seconds)  // Длительность плавного перехода (разгона) между ступенями
          .startingFrom(1)              // Начальное количество пользователей в секунду
      )
  ).//protocols(webtours.webTours.proxy(Proxy("localhost", 8888)))
    protocols(webtours.webTours)
    .maxDuration(maxDuration)
//    .assertions(
//      // Проверка для каждой группы (транзакции), определенной через group() в Actions.scala
//      details("Root Page Transaction").responseTime.max.lt(10000),
//      details("Login Transaction").responseTime.max.lt(10000),
//      details("Flight Transaction").responseTime.max.lt(10000),
//      details("One Way Ticket Transaction").responseTime.max.lt(10000),
//      details("Root Page Transaction").responseTime.max.lt(10000),
//
//      // Общая проверка на всякий случай
//      global.failedRequests.percent.is(0)
//    )
    //.assertions(
      //global.responseTime.max.lt(3000)    // Максимальное время отклика < 3000 мс
      //global.failedRequests.percent.is(0)  // Опционально: 0% ошибок
    //)
}
