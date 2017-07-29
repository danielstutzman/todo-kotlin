package dbPkg

import org.jooq.SQLDialect
import org.jooq.generated.tables.Users.USERS
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.Timestamp

data class User(
    val id: Int,
    val email: String,
    val encryptedPassword: String
)

class Db(private val conn: Connection) {

  private val create = DSL.using(conn, SQLDialect.POSTGRES_9_5)

  private fun now() = Timestamp(System.currentTimeMillis().toLong())

  fun findUserByEmail(emailAnyCase: String): User? {
    return create
        .select(USERS.ID, USERS.EMAIL, USERS.ENCRYPTED_PASSWORD)
        .from(USERS)
        .where(USERS.EMAIL.eq(emailAnyCase.toLowerCase()))
        .fetchOneInto(User::class.java)
  }

  fun createUser(emailAnyCase: String, encryptedPassword: String): Int {
    val email = emailAnyCase.toLowerCase()

    val record = create.insertInto(USERS,
        USERS.EMAIL,
        USERS.ENCRYPTED_PASSWORD,
        USERS.CREATED_AT,
        USERS.UPDATED_AT)
        .values(email, encryptedPassword, now(), now())
        .returning(USERS.ID)
        .fetchOne()
    val newId = record.getValue(USERS.ID)
    println("Created record with ID ${newId}")
    return newId
  }


//      create.delete(USERS)
//          .where(USERS.EMAIL.eq("a1"))
//          .execute()
//
//      create.select().from(USERS).fetch().let { rset ->
//        for (row in rset) {
//          val id = row.getValue(USERS.ID)
//          val email = row.getValue(USERS.EMAIL)
//          println("ID: $id EMAIL: $email")
//        }
//      }
//    }
}