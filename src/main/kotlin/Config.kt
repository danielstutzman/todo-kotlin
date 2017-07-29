import com.google.gson.Gson

data class PostgresCredentials(
    val hostname: String,
    val port: Int,
    val sslMode: String,
    val username: String,
    val password: String,
    val databaseName: String
)

data class Config(
    val port: Int,
    val postgresCredentials: PostgresCredentials
)

/** @Throws com.google.gson.JsonSyntaxException */

// suppress warning from comparing String to null
@Suppress("SENSELESS_COMPARISON")

fun parseConfigJson(json: String): Config {
  val config: Config = Gson().fromJson(json, Config::class.java)
  if (config.port == 0) {
    throw RuntimeException("Missing port")
  }
  if (config.postgresCredentials == null) {
    throw RuntimeException("Missing postgresCredentials")
  }
  if (config.postgresCredentials.hostname == null) {
    throw RuntimeException("Missing postgresCredentials.hostname")
  }
  if (config.postgresCredentials.port == null) {
    throw RuntimeException("Missing postgresCredentials.port")
  }
  if (config.postgresCredentials.sslMode == null) {
    throw RuntimeException("Missing postgresCredentials.sslMode")
  }
  if (config.postgresCredentials.databaseName == null) {
    throw RuntimeException("Missing postgresCredentials.databaseName")
  }
  if (config.postgresCredentials.username == null) {
    throw RuntimeException("Missing postgresCredentials.username")
  }
  if (config.postgresCredentials.password == null) {
    throw RuntimeException("Missing postgresCredentials.password")
  }
  return config
}

