import io.gatling.core.Predef._
import webtours.webtours

import scala.concurrent.duration.DurationInt

class CommonSimulation extends Simulation{

  val maxDuration = 2
  val maxRPS = 100.0      // Предполагаемый 100% предел (подставьте свой)
  val stepsCount = 10     // 10 шагов по 10%
  val stepIncrement = maxRPS / stepsCount // Размер одной ступени (10 RPS)
  val stepDuration = 5.seconds // Длительность одной ступени

  setUp(
    _root_.webtours.Scenarious.CommonScenario().inject(
      incrementUsersPerSec(stepIncrement) // Сколько добавлять на каждом шаге
        .times(stepsCount)                // Сколько всего шагов
        .eachLevelLasting(stepDuration)   // Длительность полки (стабильной нагрузки)
        .separatedByRampsLasting(10.seconds) // Плавный переход между ступенями
        .startingFrom(0)                  // Начинаем с 0
    )
  ).protocols(webtours.webTours)
    .maxDuration(maxDuration.minutes)
    // Критерии деградации (Assertions)
    .assertions(
      global.failedRequests.percent.lt(5), // Тест упадет, если ошибок > 5%
      global.responseTime.percentile3.lt(2000) // Тест упадет, если 95-й перцентиль > 2с
    )
}


//class CommonSimulation extends Simulation{
//
//  setUp(
//
//    scn.inject(
//      nothingFor(4.seconds),          // Пауза перед стартом
//      atOnceUsers(1),                 // Один тестовый пользователь сразу
//      rampUsers(10).during(30.seconds) // Постепенный взлет до 10 пользователей за 30 сек
//    ).protocols(webtours)
//  )
//
//}
