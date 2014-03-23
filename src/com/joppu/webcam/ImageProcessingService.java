package com.joppu.webcam;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.io.File;

public class ImageProcessingService {

    private static final ImageProcessingService imageProcessingService = new ImageProcessingService();

    private ImageProcessingService(){

    }

    public static ImageProcessingService getInstance(){
        return imageProcessingService;
    }

    public static void main(String args[]) throws Exception{
        ImageProcessingService.getInstance().start();
    }

    private void start() throws Exception{
        Runnable monitorFolder0 = new MonitorFolderRunnable("img/camera0");
        Runnable monitorFolder1 = new MonitorFolderRunnable("img/camera1");

        Thread worker0 = new Thread(monitorFolder0);
        Thread worker1 = new Thread(monitorFolder1);

        worker0.start();
        worker1.start();
    }

    private void uploadImage(File file) throws Exception{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost("http://localhost/imageupload");

            FileBody bin = new FileBody(file);

            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("file", bin)
                    .build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    private void moitorFolder(String path) throws Exception{
        Path folder = Paths.get(path);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        folder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        boolean valid = true;
        do {
            WatchKey watchKey = watchService.take();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("File Created:" + fileName);
                    processFile(path+"/"+fileName);
                }
            }
            valid = watchKey.reset();

        } while (valid);
    }

    private synchronized void processFile(String fileName) {
        try {
            uploadImage(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MonitorFolderRunnable implements Runnable{

        private final String path;

        public MonitorFolderRunnable(String path){
            this.path = path;
        }

        @Override
        public void run() {
            try {
                moitorFolder(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
