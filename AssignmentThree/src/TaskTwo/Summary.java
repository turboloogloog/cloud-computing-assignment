package TaskTwo;

import common.Tools;
import common.WriteFile;
import common.WriteHDFSFile;
import common.WriteLocalFile;
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
        double minSup=Double.parseDouble(Tools.getParameter(args,3,"0.3"));
        int iteration=Integer.parseInt(Tools.getParameter(args,4,"10"));
        String output=Tools.getParameter(args,5,"file:///result/tasktwo.txt");
        SparkConf conf=new SparkConf().setAppName("Summary Application").setMaster("local[2]").set("spark.executor.memory","1g");
        JavaSparkContext sc=new JavaSparkContext(conf);
        JavaPairRDD<String,String> geoData=sc.textFile(geo).filter((Function<String, Boolean>) s -> {
            if (s==null||s.matches("\\s*")){
                return false;
            }
            String[] values=s.split(",");
            return values.length >= 3 && values[2] != null && !"expression_value".equals(values[2].trim()) && Double.parseDouble(values[2]) > 1250000;
        }).mapToPair((PairFunction<String, String, String>) s -> {
            String[] values=s.split(",");
            return new Tuple2<>(values[0],values[1]);
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
        Map<String,Iterable<Tuple2<String,String>>> temp=geoData.join(patientData).groupByKey().collectAsMap();
        Set<String> set=temp.keySet();
        WriteFile writeFile=null;
        String transaFile=Tools.createTempFile();
        try {
            writeFile=new WriteLocalFile(transaFile);
            for (String s:set){
                Iterable<Tuple2<String,String>> ita=temp.get(s);
                for (Tuple2<String,String> tp:ita){
                    writeFile.write(String.format("%s\n",tp._1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writeFile!=null){
                writeFile.tearDown();
            }
        }
        try {
            Map<Integer, ArrayList<Integer>> aresult=new Apriori(transaFile,minSup,iteration).getResult();
            ArrayList<Tuple2<Integer,ArrayList<Integer>>> tt=new ArrayList<>(aresult.size());
            Set<Integer> keySet=aresult.keySet();
            for (Integer i:keySet){
                tt.add(new Tuple2<>(i,aresult.get(i)));
            }
            tt.sort(Comparator.comparingInt(o -> o._1));
            int max=tt.size()-1;
            writeFile=new WriteHDFSFile(output);// hdfs://host:port/file path
            for (int i=max;i>-1;i--){
                StringBuilder stringBuilder=new StringBuilder();
                Tuple2<Integer,ArrayList<Integer>> tuple2=tt.get(i);
                stringBuilder.append(String.format("%s\t",tuple2._1));
                ArrayList<Integer> arrayList=tuple2._2;
                for (Integer integer:arrayList){
                    stringBuilder.append(String.format("%s\t",integer));
                }
                writeFile.write(stringBuilder.toString()+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (writeFile!=null) {
                writeFile.tearDown();
            }
        }
        sc.close();
        if (writeFile!=null){
            writeFile.release();
        }
    }

}