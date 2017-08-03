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

data class ThreadData(
    val steps: LinkedList<Step>,
    var userId: Int?
)

object ReqLog {
  private val logger = LoggerFactory.getLogger(ReqLog::class.java)

  val threadDatas = object : ThreadLocal<ThreadData>() {
    override fun initialValue(): ThreadData {
      return ThreadData(LinkedList<Step>(), null)
    }
  }

  fun start(req: Request) {
    val reqId = nextRequestId.getNextRequestId()
    val call = Thread.currentThread().getStackTrace()[2]
    val threadData = threadDatas.get()
    threadData.steps.push(Step(
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
            "protocol=${req.protocol()}"

    ))
  }

  fun setUserId(userId: Int?) {
    threadDatas.get().userId = userId
  }

  fun start() {
    val localSteps = threadDatas.get().steps
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
    val threadData = threadDatas.get()
    if (threadData.steps.size > 0) {
      val step = threadData.steps.pop()
      val millis = System.currentTimeMillis() - step.startMillis
      val reqSpecific =
          if (step.reqParts != null)
            "${step.reqParts} userId=${threadData.userId} "
          else
            ""
      logger.info(reqSpecific +
          "call=${step.className}.${step.methodName} " +
          "ms=${millis} " +
          "reqId=${step.reqId}")
    }
  }
}