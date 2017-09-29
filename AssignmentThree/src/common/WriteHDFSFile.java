package common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;


public class WriteHDFSFile extends WriteFile{

    private FileSystem fileSystem;
    private FSDataOutputStream fsDataOutputStream;

    public WriteHDFSFile(String path) throws IOException {
        super(path);
        URI uri=URI.create(path);
        Configuration conf=new Configuration();
        fileSystem=FileSystem.get(uri, conf);
        fsDataOutputStream=fileSystem.create(new Path(uri));
    }

    @Override
    public void write(String str) {
        try {
            fsDataOutputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tearDown() {
        if (fsDataOutputStream!=null){
            try {
                fsDataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void release() {
        try {
            if (fileSystem!=null){
                fileSystem.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
