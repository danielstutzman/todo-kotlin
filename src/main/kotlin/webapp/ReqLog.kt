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
    val method: String?,
    val path: String?) {
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
        req.requestMethod(),
        req.pathInfo()
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
          null,
          null
      ))
    }
  }

  fun finish() {
    val localSteps = steps.get()
    if (localSteps.size > 0) {
      val step = localSteps.pop()
      val millis = System.currentTimeMillis() - step.startMillis
      val methodAndPath =
          if (step.method != null)
            "method=${step.method} path=${step.path} "
          else
            ""
      logger.info("${methodAndPath}call=${step.className}.${step.methodName} ms=${millis} reqId=${step.reqId}")
    }
  }
}