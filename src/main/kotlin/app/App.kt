package app

import db.Db

class App(
    val db: Db,
    val passwordHasher: PasswordHasher,
    val tokenGenerator: SecureTokenGenerator
) {}



