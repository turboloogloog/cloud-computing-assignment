package common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;


public abstract class WriteFile {

    public WriteFile(String path) throws IOException {
    }

    public abstract void write(String str);

    public abstract void tearDown();

    public abstract void release();

}
