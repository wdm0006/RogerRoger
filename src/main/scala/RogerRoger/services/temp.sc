import dispatch._, Defaults._

import scala.concurrent.Await
import scala.concurrent.duration._

val foobar = url("http://google.com")

val request = dispatch.Http.apply(foobar.GET)
