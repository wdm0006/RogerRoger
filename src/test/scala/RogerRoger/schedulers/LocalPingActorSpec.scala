package RogerRoger.schedulers

import org.scalatest.FunSuite

class LocalPingActorSpec extends FunSuite {
  private class TrackedSource(text: String) extends scala.io.Source {
    var closed = false
    override def close(): Unit = closed = true
    override def reset() = this
    override val iter: Iterator[Char] = text.iterator
  }

  test("consume returns the full body and closes the source") {
    val src = new TrackedSource("""{"services":["/stats/top"]}""")
    assert(LocalPingActor.consume(src) === """{"services":["/stats/top"]}""")
    assert(src.closed)
  }

  test("consume closes the source when reading throws") {
    var closed = false
    val failing = new scala.io.Source {
      override def close(): Unit = closed = true
      override def reset() = this
      val iter: Iterator[Char] = new Iterator[Char] {
        def hasNext = true
        def next(): Char = throw new java.io.IOException("boom")
      }
    }
    intercept[java.io.IOException] { LocalPingActor.consume(failing) }
    assert(closed)
  }
}
