# How to run web server

`mvn compile && java -cp $(cat .mvn-classpath):target/classes HelloKt`

(Optionally, run `./fastbuild` instead of `mvn compile` or `mvn test-compile`)

# How to run test

`mvn test-compile && java -cp $(cat .mvn-classpath):target/classes:target/test-classes IntegrationTest`

(Optionally, run `./fastbuild` instead of `mvn compile` or `mvn test-compile`)
