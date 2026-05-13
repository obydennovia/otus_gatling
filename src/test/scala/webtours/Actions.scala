package webtours

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Actions {

  // Очистка Cookie
  val clearCookiesAction = flushCookieJar

  // Querry-параметры
  val querryParams = Map(
    "username" -> "oia",
    "password" -> "888888",
    "numPassengers" -> "1",
    "firstName" -> "Ivan",
    "lastName" -> "Obydennov",
    "address1" -> "Street",
    "address2" -> "City",
    "pass1" -> "Ivan Obydennov",
    "creditCard" -> "1234",
    "expDate" -> "12/3000"
  )

  // Общие заголовки (выносим повторяющиеся части)
  val baseHeaders = Map(
    "Accept-Language" -> "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "Upgrade-Insecure-Requests" -> "1",
    "Priority" -> "u=4",
    "Accept-Encoding" -> "gzip, deflate",
    "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0",
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
  )
  /* ---------------------------------------------- */
  /* --- Begin Root Page Transaction Controller --- */
  /* ---------------------------------------------- */

  // Вход на RootPage
  val enterRootPage = http("Root Page /cgi-bin/welcome.pl")
    .get("/cgi-bin/welcome.pl")
    .queryParam("signOff", "true")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/webtours/"))
    .check(status.is(200))

  // Получение UserSession
  val getUserSession = http("Root Page /cgi-bin/nav.pl")
    .get("/cgi-bin/nav.pl")
    .queryParam("in", "home")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/welcome.pl?signOff=true"))
    .check(css("input[name='userSession']", "value").saveAs("userSession"))
    .check(status.is(200))

  // Группировка (Root Page Transaction Controller)
  val rootPageTransaction = group("Root Page Transaction") {
    exec(enterRootPage)
      .exec(getUserSession)
  }
  /* -------------------------------------------- */
  /* --- End Root Page Transaction Controller --- */
  /* -------------------------------------------- */

  /* ------------------------------------------ */
  /* --- Begin Login Transaction Controller --- */
  /* ------------------------------------------ */

  // Начало авторизации
  val beginAuthorization = http("Login /cgi-bin/login.pl")
    .post("/cgi-bin/login.pl")
    .queryParam("in", "home")
    .formParam("userSession", "#{userSession}")
    //.formParam("username", querryParams("username"))
    //.formParam("password", querryParams("password"))
    .formParam("username", "#{username}")
    .formParam("password", "#{password}")
    .formParam("login.x", "74")
    .formParam("login.y", "4")
    .formParam("JSFormSubmit", "off")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/nav.pl?in=home"))
    .headers(baseHeaders + ("Origin" -> "http://webtours.load-test.ru:1080"))
    .headers(baseHeaders + ("Upgrade-Insecure-Requests" -> "1"))
    .headers(baseHeaders + ("Content-Type" -> "application/x-www-form-urlencoded"))
    .check(status.is(200))

  // Проверка UserSession
  val checkAuthorization = http("Login /cgi-bin/nav.pl")
    .get("/cgi-bin/nav.pl")
    .queryParam("page", "menu")
    .queryParam("in", "home")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/login.pl"))
    .check(status.is(200))

  // Завершение авторизации
  val endAuthorization = http("Login /cgi-bin/login.pl")
    .get("/cgi-bin/login.pl")
    .queryParam("intro", "true")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/login.pl"))
    .check(status.is(200))

  // Группировка (Root Page Transaction Controller)
  val loginTransaction = group("Login Transaction") {
    exec(beginAuthorization)
      .exec(checkAuthorization)
      .exec(endAuthorization)
  }

  /* ---------------------------------------- */
  /* --- End Login Transaction Controller --- */
  /* ---------------------------------------- */

  /* ------------------------------------------- */
  /* --- Begin Flight Transaction Controller --- */
  /* ------------------------------------------- */

  // Вход на страницу Flight
  val getFlightPage = http("Flight /cgi-bin/welcome.pl")
    .get("/cgi-bin/welcome.pl")
    .queryParam("page", "search")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/nav.pl?page=menu&in=flights"))
    .check(status.is(200))

  // Поиск рейса
  val searchFlightPage = http("Flight /cgi-bin/nav.pl")
    .get("/cgi-bin/nav.pl")
    .queryParam("page", "menu")
    .queryParam("in", "flights")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/welcome.pl?page=search"))
    .check(status.is(200))

  // Получение списка городов
  val getFlightCities = http("Flight /cgi-bin/reservations.pl")
    .get("/cgi-bin/reservations.pl")
    .queryParam("page", "welcome")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/welcome.pl?page=search"))
    .check(regex("""<option.*?value=".*?">(.*?)<\/option>""").findAll.saveAs("cityList"))
    .check(regex("""name="departDate"\s+value="([^"]+)"""").find.saveAs("departDate"))
    .check(regex("""name="returnDate"\s+value="([^"]+)"""").find.saveAs("returnDate"))
    .check(status.is(200))

  // Группировка (Flight Transaction Controller)
  val flightTransaction = group("Flight Transaction") {
    exec(getFlightPage)
      .exec(searchFlightPage)
      .exec(getFlightCities)
  }

  /* ----------------------------------------- */
  /* --- End Flight Transaction Controller --- */
  /* ----------------------------------------- */

  /* --------------------------------------------------- */
  /* --- Begin One Way Ticket Transaction Controller --- */
  /* --------------------------------------------------- */

  // Выбор городов отправления/прибытия и даты отправления/прибытия
  val selectCitiesAndDates = http("One Way Ticket /cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .formParam("advanceDiscount", "0")
    .formParam("depart", "#{cityList.random()}")
    .formParam("departDate", "#{departDate}")
    .formParam("arrive", "#{cityList.random()}")
    .formParam("returnDate", "#{returnDate}")
    .formParam("numPassengers", querryParams("numPassengers"))
    .formParam("seatPref", "None")
    .formParam("seatType", "Coach")
    .formParam("findFlights.x", "42")
    .formParam("findFlights.y", "6")
    .formParam(".cgifields", "roundtrip")
    .formParam(".cgifields", "seatType")
    .formParam(".cgifields", "seatPref")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/reservations.pl?page=welcome"))
    .headers(baseHeaders + ("Origin" -> "http://webtours.load-test.ru:1080"))
    .headers(baseHeaders + ("Upgrade-Insecure-Requests" -> "1"))
    .headers(baseHeaders + ("Content-Type" -> "application/x-www-form-urlencoded"))
    .check(regex("""name="outboundFlight" value="([^"]+)"""").findAll.saveAs("outboundFlights"))
    .check(status.is(200))

  // Выбор рейса отправления
  val selectOutboundFlights = http("One Way Ticket /cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .formParam("outboundFlight", "#{outboundFlights.random()}")
    .formParam("numPassengers", querryParams("numPassengers"))
    .formParam("advanceDiscount", "0")
    .formParam("seatType", "Coach")
    .formParam("seatPref", "None")
    .formParam("reserveFlights.x", "55")
    .formParam("reserveFlights.y", "6")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/reservations.pl"))
    .headers(baseHeaders + ("Origin" -> "http://webtours.load-test.ru:1080"))
    .headers(baseHeaders + ("Content-Type" -> "application/x-www-form-urlencoded"))
    .check(status.is(200))

  // Оплата билета
  val postPayment = http("One Way Ticket /cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .formParam("firstName", querryParams("firstName"))
    .formParam("lastName", querryParams("lastName"))
    .formParam("address1", querryParams("address1"))
    .formParam("address2", querryParams("address2"))
    .formParam("pass1", querryParams("pass1"))
    .formParam("creditCard", querryParams("creditCard"))
    .formParam("expDate", querryParams("expDate"))
    .formParam("oldCCOption", "")
    .formParam("numPassengers", querryParams("numPassengers"))
    .formParam("seatType", "Coach")
    .formParam("seatPref", "None")
    .formParam("outboundFlight", "#{outboundFlights.random()}")
    .formParam("advanceDiscount", "0")
    .formParam("returnFlight", "")
    .formParam("JSFormSubmit", "off")
    .formParam("buyFlights.x", "73")
    .formParam("buyFlights.y", "16")
    .formParam(".cgifields", "saveCC")
    .headers(baseHeaders + ("Referer" -> "http://webtours.load-test.ru:1080/cgi-bin/reservations.pl"))
    .headers(baseHeaders + ("Origin" -> "http://webtours.load-test.ru:1080"))
    .headers(baseHeaders + ("Content-Type" -> "application/x-www-form-urlencoded"))
    .check(status.is(200))

  // Группировка (One Way Ticket Transaction Controller)
  val oneWayTicketTransaction = group("One Way Ticket Transaction") {
    exec(selectCitiesAndDates)
      .exec(selectOutboundFlights)
      .exec(postPayment)
  }

  /* ------------------------------------------------- */
  /* --- End One Way Ticket Transaction Controller --- */
  /* ------------------------------------------------- */

}
