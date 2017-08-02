# How to run web server

`mvn compile && java -cp $(cat .mvn-classpath):target/classes WebServerKt`

Afterwards you can just run `./fastbuild serve`

# How to run integration test

`./fastbuild scrape` to scrape from old server and save output

`./fastbuild compare` to scrape from new server and compare to saved output

# How to delete database

`mvn flyway:clean`

# How to run migations

`mvn flyway:migrate jooq-codegen:generate`
