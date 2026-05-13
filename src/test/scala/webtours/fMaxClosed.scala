package webtours

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class fMaxClosed extends Simulation {

  val maxDuration = 70  // Длительность теста в секундах
  setUp(
    Scenarious.CommonScenario() // Что делает пользователь
      .inject(
        //atOnceUsers(1)
        incrementConcurrentUsers(5)     // Добавить по 5 пользователей/сек. на каждой ступени
          .times(4)                     // Количество ступеней увеличения
          .eachLevelLasting(5.seconds)  // Длительность ступени
          .separatedByRampsLasting(10)  // Длительность плавного перехода (разгона) между ступенями
          .startingFrom(0)              // Начальное количество пользователей в секунду
      )
  ).//protocols(webtours.webTours.proxy(Proxy("localhost", 8888)))
    protocols(webtours.webTours)
    .maxDuration(maxDuration)
//    .assertions(
//      global.failedRequests.count.is(0),                                // ни одной ошибки
//      global.responseTime.percentile(95).lt(800),                       // p95 < 800 ms
//      forAll.responseTime.max.lt(2000),                                 // ни у одного запроса max > 2 sec
//      details(("GET /api/items")).successfulRequests.percent.gte(99.5), // для конкретного запроса
//      global.requestsPerSec.gte(100)                                    // средний RPS не менее 100
//    )
}
