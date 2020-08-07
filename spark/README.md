1. **Requirements**
   - [sbt](https://www.scala-sbt.org/)
   - [wget](https://www.tecmint.com/install-wget-in-linux/)
   - [spark (2.4.6)](https://spark.apache.org/downloads.html)
   - [nc](https://linuxize.com/post/netcat-nc-command-with-examples/#:~:text=Netcat%20(or%20nc%20)%20is%20a,army%20knife%20of%20networking%20tools.)
   - It was not mentioned, but we use [sdkman](https://sdkman.io/) to install the most part of the requirements
   
2. **Create the project folder structure**
```bash
mkdir <project_name>/
mkdir -p <project_name>/src/main/scala
```

3. **Copy the file StructuredNetworkWordCountWindowed.scala to the <project_name>/src/main/scala folder**
```bash
cd <project_name>/src/main/scala
wget https://raw.githubusercontent.com/apache/spark/v3.0.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredNetworkWordCountWindowed.scala
```

4. **In order to avoid unnecessary logs in the console, we are going to set the logs to be shown only when some error occurs. For that you have to set the Log level to `ERROR`**
   - Add the following code after the spark session being declared: 
   ```scala
   spark.sparkContext.setLogLevel("ERROR")
   ```
   
   ```scala
   ...
   val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCountWindowed")
      .getOrCreate()
   //TODO: here you add the code:
   spark.sparkContext.setLogLevel("ERROR")
   import spark.implicits._
   ...
   ```

   - For testing purpose, it was created the file <project_name>/src/main/scala/Hello.scala wirh the following content:
   ```scala
   package foo.bar.
   object Main extends App { 
      println("Hello, world") 
   }
   ```
   
5. **Create the build.sbt file with the following contend**
```bash
cd <project_name>/
touch build.sbt
```
   - contend
   ```sbt
   name := "NO_IDE_PROJECT"
   scalaVersion := "2.11.12"
   version := "3.0"

   libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.6"
   libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.6"
   libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.6"
   ```

6. **Compiling and Packaging the project**
```bash
cd <project_name>/
sbt package
```

7. **Running the applications**

   - Executing the `Hello.scala`:
   ```bash
   /opt/spark/bin/spark-submit --class foo.bar.baz.Main <project_name>/target/scala-2.11/no_ide_project_2.11-3.0.jar
   ```

   - Executing the `StructuredNetworkWordCountWindowed.scala`

      - Open up a new console window and execute the `nc` command:
      ```bash
      nc -lk 9999
      ```
      - Open up a new console window and execute the spark programm: 
      ```bash
      /opt/spark/bin/spark-submit --class foo.bar.baz.StructuredNetworkWordCountWindowed <project_name>/target/scala-2.11/no_ide_project_2.11-3.0.jar "localhost" "9999" "10" "5"
      ```  
      - Back into the `nc` window, you can type messages and press `enter`, the messages will be listen by the spark job and the words will be counted.

:white_check_mark:
