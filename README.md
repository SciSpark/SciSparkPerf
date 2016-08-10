SciSparkPerf
============

[![Join the chat at https://gitter.im/scientificspark](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scientificspark/Lobby)

#Introduction

SciSparkPerf is a framework used to benchmark SciSpark and it's applications.
[SciSpark](http://esto.nasa.gov/forum/estf2015/presentations/Mattmann_S1P8_ESTF2015.pdf) is a scalable scientific processing platform that makes interactive computation and exploration possible.
* We assumes you have followed the instructions to install and get SciSpark running. Otherwise you can find the instructions [here](https://github.com/SciSpark/SciSpark).
* Join the SciSpark gitter chat if you have general questions about setting up SciSparkPerf or SciSpark.

#Installation - standalone mode
The following instructions detail how to run the benchmarking suite as-is.
If you want to benchmark the suite in clustered mode with data on hdfs go to the cluster mode installation

1. After you have installed SciSpark, go to the SciSpark directory

2. Have sbt publish the project to the local ivy repository so we can use it in the benchmark. 
    ```
    sbt publishLocal
    ```

3. Clone this repository
    ```
    git clone https://github.com/rahulpalamuttam/SciSparkPerf
    ```
    
4. To run the suite execute
    ```
    ./run.sh
    ```

5. The results for each of the methods can be found in a csv file named results.csv


#Installation - cluster mode
The following instructions detail how to run the benchmarking suite in clustered mode.

1. Follow steps 1 - 5 in the standalone mode instructions

2. If you haven't already built your jar now is the time to do so.
   Within your SciSpark folder, run ```sbt clean assembly```
   
3. Set the fs.base.path to point to your hdfs uri
   Example hdfs uri: hdfs://HOSTNAME:8090/data/resources

4. Note that the dataset in the tests are located in src/jmh/resources
   Each folder's dataset size is denoted by their name - 100mb, 1gb, 10gb, 100gb
   In standalone each file just contains the same 4 files.
   You can put this on hdfs by executing
    ```
    hadoop fs -put src/jmh/resources /
    ```


5. For testing purposes upload your own data into these directories
   Try to put enough data to meet the sizes indicated by the folder names
   

6. set the spark master uri by setting the spark.master properties in the properties file
   An example URI: spark://HOSTNAME:7077
   AN example mesos URI: mesos//HOSTNAME:5050

7. The test effectively uses spark as a client. To be able to use it in this mode,
   we need to set the executor uri to point to the spark-version.tgz file.
   You can either set it to a http uri (where you downloaded your spark binary from.
   You can also put it into hdfs and set the property as an hdfs uri.
   An example http url is : http://d3kbcqa49mib13.cloudfront.net/spark-1.6.2-bin-hadoop2.6.tgz
   An example hdfs uri is : hdfs://HOSTNAME:8090/spark/spark-1.6.0-bin-hadoop2.4.tgz
   
   
8. Find where your SciSpark.jar is and take note of its path as follows /path_to_SciSpark/target/scala-2.10/SciSpark.jar.

9. Add the jar path as the scispark.jar property in the Properties file.

10. To run the suite execute
    ```
    ./run.sh
    ```
 

#Getting Started



#Running the test


#API

##Project Status



##Want to Use or Contribute to this Project?
Contact us at [scispark-team@jpl.nasa.gov](mailto:scispark-team@jpl.nasa.gov)

##Technology
This project utilizes the following open-source technologies [Apache Spark][Spark] and is based on the NASA AIST14 project led by the PI [Chris Mattmann](http://github.com/chrismattmann/). The SciSpark team is at at JPL and actively working on the project.

###Apache Spark

[Apache Spark][Spark] is a framework for distributed in-memory data processing. It runs locally, on an in-house or commercial cloud environment.

[Spark]: https://spark.apache.org/
