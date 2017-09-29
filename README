=================Task One====================

wget http://www.fbn.com.au/AssignmentThree.zip
unzip AssignmentThree.zip
cp AssignmentThree_old/build.xml AssignmentThree
cp AssignmentThree_old/pom.xml AssignmentThree
cd AssignmentThree
mkdir src/main
mkdir src/main/java
cp src/common/ src/main/java/ -r
rm src/main/java/* -r
cp src/TaskOne/Summary.java src/main/java/
mvn -Pyarn -Phadoop-2.7 -Dhadoop.version=2.7.2 -Phive -Phive-thriftserver -DskipTests clean package
mv target/spark-0.0.1.jar AssignmentThree_1.jar

/usr/local/spark/bin/spark-submit \
--class TaskOne.Summary \
--master yarn \
--num-executors 3 \
AssignmentThree_1.jar \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/share/genedata/small/GEO.txt \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/share/genedata/small/PatientMetaData.txt \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/user/qzen9084/spark /

hdfs dfs -cat spark


=================Task Two====================

cp src/TaskTwo/* src/main/java/ -f
mvn -Pyarn -Phadoop-2.7 -Dhadoop.version=2.7.2 -Phive -Phive-thriftserver -DskipTests clean package
mv target/spark-0.0.1.jar AssignmentThree_2.jar

/usr/local/spark/bin/spark-submit \
--class TaskTwo.Summary \
--master yarn \
--num-executors 3 \
AssignmentThree_2.jar \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/share/genedata/large/GEO.txt \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/share/genedata/large/PatientMetaData.txt \
0.005 \
10 \
hdfs://soit-hdp-pro-1.ucc.usyd.edu.au:8020/user/qzen9084/spark2 /

hdfs dfs -cat spark2