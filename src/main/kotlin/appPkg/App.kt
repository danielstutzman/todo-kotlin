package appPkg

import dbPkg.Db

class App(
    val db: Db,
    val passwordHasher: SecurePasswordHasher,
    val tokenGenerator: SecureTokenGenerator
) {}



