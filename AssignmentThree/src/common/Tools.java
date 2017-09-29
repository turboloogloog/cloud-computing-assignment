package common;

import java.util.UUID;


public class Tools {

    public static String getParameter(String[] args,int index,String deft){
        if (args.length<index){
            return deft;
        }
        return args[index-1];
    }

    public static String createTempFile(){
        return String.format("temp/temp-%s.txt", UUID.randomUUID().toString().replace("-",""));
    }

}
