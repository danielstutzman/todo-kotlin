import com.google.gson.Gson


data class PostgresCredentials(
    val hostname: String,
    val port: Int,
    val sslMode: String,
    val username: String,
    val password: String,
    val databaseName: String
)

data class PostgresCredentialsNulls(
    val hostname: String?,
    val port: Int?,
    val sslMode: String?,
    val username: String?,
    val password: String?,
    val databaseName: String?
) {
  fun toPostgresCredentials() = PostgresCredentials(
      hostname ?: throw RuntimeException("Missing hostname"),
      port ?: throw RuntimeException("Missing port"),
      sslMode ?: throw RuntimeException("Missing sslMode"),
      username ?: throw RuntimeException("Missing username"),
      password ?: throw RuntimeException("Missing password"),
      databaseName ?: throw RuntimeException("Missing databaseName"))
}


data class Config(
    val port: Int,
    val postgresCredentials: PostgresCredentials
)

data class ConfigNulls(
    val port: Int?,
    val postgresCredentials: PostgresCredentialsNulls?
) {
  fun toConfig() = Config(
      port ?: throw RuntimeException("Missing port"),
      if (postgresCredentials != null)
        postgresCredentials.toPostgresCredentials()
      else
        throw RuntimeException("Missing postgresCredentials")

  )
}

/** @Throws com.google.gson.JsonSyntaxException */
fun parseConfigJson(json: String): Config {
  val configNulls: ConfigNulls =
      Gson().fromJson(json, ConfigNulls::class.java)
  return configNulls.toConfig()
}

