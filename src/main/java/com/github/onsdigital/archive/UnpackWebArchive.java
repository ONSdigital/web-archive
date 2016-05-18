package com.github.onsdigital.archive;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.apache.commons.configuration.FileSystem;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by iankent on 18/05/2016.
 */
public class UnpackWebArchive {
    public static void main(String[] args) throws IOException {
        ARCReader reader = ARCReaderFactory.get("data/IAH-20080430204825-00000-blackbook.arc.gz");
        reader.forEach(new Consumer<ArchiveRecord>() {
            public void accept(ArchiveRecord archiveRecord) {
                String url = archiveRecord.getHeader().getUrl();
                if(url.startsWith("http://www.archive.org")) {
                    System.out.println(url);
                    String outputPath = "output/" + url.substring("http://www.archive.org".length());
                    System.out.println(outputPath);
                    if(outputPath.endsWith("//")) {
                        outputPath = outputPath.substring(0, outputPath.length() - 2) + "/index.html";
                    }
                    File f = new File(outputPath);
                    if(f.getParentFile() != null) {
                        f.getParentFile().mkdirs();
                    }
                    try {
                        f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(f);
                        int c;
                        int rs = 0;
                        int ns = 0;
                        boolean headersSkipped = !archiveRecord.hasContentHeaders();
                        while((c = archiveRecord.read()) != -1) {
                            if (!headersSkipped) {
                                if(c == '\n') {
                                    ns++;
                                } else if(c == '\r') {
                                    rs++;
                                } else {
                                    ns = 0;
                                    rs = 0;
                                }
                                if(ns == 2 && rs == 2) {
                                    headersSkipped = true;
                                }
                                continue;
                            }
                            fos.write(c);
                        }
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
