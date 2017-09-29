package common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class WriteLocalFile extends WriteFile{

    private PrintWriter printWriter=null;
    private FileWriter fileWriter=null;

    public WriteLocalFile(String path) throws IOException {
        super(path);
        this.fileWriter=new FileWriter(path);
        this.printWriter=new PrintWriter(fileWriter);;
    }

    @Override
    public void write(String str) {
        printWriter.write(str);
    }

    @Override
    public void tearDown() {
        if (fileWriter!=null){
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (printWriter!=null){
            printWriter.close();
        }
    }

    @Override
    public void release() {
    }

}
