1. **Requirements**
   - [sbt](https://www.scala-sbt.org/)
   - [wget](https://www.tecmint.com/install-wget-in-linux/)
   - [spark (2.4.6)](https://spark.apache.org/downloads.html)
   - [nc](https://linuxize.com/post/netcat-nc-command-with-examples/#:~:text=Netcat%20(or%20nc%20)%20is%20a,army%20knife%20of%20networking%20tools.)
   - It was not mentioned, but we use [sdkman](https://sdkman.io/) to install the most part of the requirements
   
2. **Clone the repository**
```bash
git clone git@github.com:zioli/studies.git
```

3. **Position yourself on the rigth folder**
```bash
cd ./studies/spark-streaming/
```

4. **Compiling and Packaging the project**
```bash
sbt package
```

5. **Testing the application**

   - Executing the `WordCountWithStateAndTimeout.scala`

   - Open up a new console window and execute the `nc` command:
   ```bash
   nc -lk 9999
   ```
   - Open up a new console window and execute the spark programm: 
   ```bash
   /opt/spark/bin/spark-submit --class src.main.scala.WordCountWithStateAndTimeout target/scala-2.11/spark-streaming_2.11-3.0.jar "localhost" "9999"
   ```  
   - Back into the `nc` window, you can type messages and press `enter`, the messages will be listen by the spark job and the words will be counted.

   - Messages:
   ```
   2020-08-04 10:00:00.000,message numero 1
   2020-08-04 10:00:05.000,message numero 2
   2020-08-04 10:00:10.000,message numero 3
   2020-08-04 10:00:15.000,message numero 4
   2020-08-04 10:00:20.000,message numero 5
   2020-08-04 10:00:25.000,message numero 6
   2020-08-04 10:00:30.000,message numero 7
   2020-08-04 10:00:35.000,message numero 8
   2020-08-04 10:00:40.000,message numero 9
   2020-08-04 10:00:45.000,message numero 10
   2020-08-04 11:00:55.000,message numero 11
   2020-08-04 11:00:05.000,message numero 12
   ```
   
:white_check_mark:
