package TaskOne;

import common.Tools;
import common.WriteFile;
import common.WriteHDFSFile;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class Summary {

    private static Pattern pattern = Pattern.compile("(prostate-cancer)|(breast-cancer)|(pancreatic-cancer)|(leukemia)|(lymphoma)");

    public static void main(String[] args){
        String geo=Tools.getParameter(args,1,"test/GEO.txt");
        String patient=Tools.getParameter(args,2,"test/PatientMetaData.txt");
        String output=Tools.getParameter(args,3,"file:///result/taskone.txt");
        SparkConf conf=new SparkConf().setAppName("Summary Application").setMaster("local[2]").set("spark.executor.memory","1g");
        JavaSparkContext sc=new JavaSparkContext(conf);
        JavaPairRDD<String,Integer> geoData=sc.textFile(geo).filter((Function<String, Boolean>) s -> {
            if (s==null||s.matches("\\s*")){
                return false;
            }
            String[] values=s.split(",");
            return values.length >= 3 && values[2] != null && "42".equals(values[1]) && Double.parseDouble(values[2]) > 1250000;
        }).mapToPair((PairFunction<String, String, Integer>) s -> {
            String[] values=s.split(",");
            return new Tuple2<>(values[0],1);
        });
        JavaPairRDD<String,String> patientData=sc.textFile(patient).filter((Function<String, Boolean>) s -> {
            if (s==null||s.matches("\\s*")){
                return false;
            }
            String[] values=s.split(",");
            return values.length >= 6 && pattern.matcher(values[4]).find();
        }).flatMapToPair((PairFlatMapFunction<String, String, String>) s -> {
            String[] values=s.split(",");
            String[] diseases=values[4].split(" ");
            List<Tuple2<String, String>> allRow=new ArrayList<>(diseases.length);
            for (String d:diseases){
                if (pattern.matcher(d).find()){
                    allRow.add(new Tuple2<>(values[0],d));
                }
            }
            return allRow.iterator();
        });
        Map<String,Long> temp=geoData.join(patientData).map((Function<Tuple2<String, Tuple2<Integer, String>>, String>) stringTuple2Tuple2 -> stringTuple2Tuple2._2()._2).countByValue();
        List<String> result=new ArrayList<>(temp.size());
        Set<String> set=temp.keySet();
        for (String key : set) {
            result.add(String.format("%s\t%s", key, temp.get(key)));
        }
        result.sort((o1, o2) -> {
            String[] o1tm=o1.split("\t");
            String[] o2tm=o2.split("\t");
            long o1t=Long.parseLong(o1tm[1]);
            long o2t=Long.parseLong(o2tm[1]);
            if (o1t<o2t){
                return -1;
            }else if (o1t>o2t){
                return 1;
            }else{
                int cc=o1tm[0].compareTo(o2tm[0]);
                if (cc>0){
                    return -1;
                }else if (cc<0){
                    return 1;
                }else{
                    return 0;
                }
            }
        });
        int max=result.size()-1;
        WriteFile writeFile=null;
        try {
            // hdfs://host:port/file path
            writeFile=new WriteHDFSFile(output);
            for (int i=max;i>-1;i--){
                writeFile.write(String.format("%s\n",result.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writeFile!=null){
                writeFile.tearDown();
            }
        }
        sc.close();
        if (writeFile!=null){
            writeFile.release();
        }
    }

}