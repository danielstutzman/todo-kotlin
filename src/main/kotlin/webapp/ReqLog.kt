package webapp

import spark.Request
import java.util.LinkedList

object nextRequestId {
  var _nextRequestId: Int = 1

  fun getNextRequestId(): Int {
    synchronized(this) {
      _nextRequestId += 1
      return _nextRequestId
    }
  }
}

data class Step(
    val className: String,
    val methodName: String,
    val startMillis: Long,
    val reqId: Int) {
}

object ReqLog {
  val steps = object : ThreadLocal<LinkedList<Step>>() {
    override fun initialValue(): LinkedList<Step> {
      return LinkedList<Step>()
    }
  }

  fun start(req: Request) {
    val reqId = nextRequestId.getNextRequestId()
    val call = Thread.currentThread().getStackTrace()[2]
    steps.get().push(Step(
        call.className.substringAfterLast("."),
        call.methodName,
        System.currentTimeMillis(),
        reqId
    ))
  }

  fun start() {
    val localSteps = steps.get()
    val call = Thread.currentThread().getStackTrace()[2]
    localSteps.push(Step(
        call.className.substringAfterLast("."),
        call.methodName,
        System.currentTimeMillis(),
        localSteps.last.reqId
    ))
  }

  fun finish() {
    val step = steps.get().pop()
    val millis = System.currentTimeMillis() - step.startMillis
    println("call=${step.className}.${step.methodName} ms=${millis} reqId=${step.reqId}")
  }
}