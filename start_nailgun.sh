#!/bin/bash
java -Xmx256M -Xms32M -noverify -cp ~/dev/nailgun-nailgun-all-0.9.1/nailgun-server/target/classes:/Users/dan/dev/kotlinc/lib/kotlin-preloader.jar:/Users/dan/dev/kotlinc/lib/kotlin-compiler.jar com.martiansoftware.nailgun.NGServer
