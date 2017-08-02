package webapp

import org.slf4j.LoggerFactory
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
    val reqId: Int,
    val reqParts: String?) {
}

object ReqLog {
  private val logger = LoggerFactory.getLogger(ReqLog::class.java)

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
        reqId,
        "scheme=${req.scheme()} " +
            "host=${req.host()} " +
            "method=${req.requestMethod()} " +
            "path=${req.pathInfo()} " +
            "query=\"${req.queryString()}\" " +
            "userAgent=\"${req.userAgent()}\" " +
            "protocol=${req.protocol()} "

    ))
  }

  fun start() {
    val localSteps = steps.get()
    if (localSteps.size > 0) {
      val call = Thread.currentThread().getStackTrace()[2]
      localSteps.push(Step(
          call.className.substringAfterLast("."),
          call.methodName,
          System.currentTimeMillis(),
          localSteps.last.reqId,
          null
      ))
    }
  }

  fun finish() {
    val localSteps = steps.get()
    if (localSteps.size > 0) {
      val step = localSteps.pop()
      val millis = System.currentTimeMillis() - step.startMillis
      logger.info("${step.reqParts ?: ""}call=${step.className}.${step.methodName} ms=${millis} reqId=${step.reqId}")
    }
  }
}