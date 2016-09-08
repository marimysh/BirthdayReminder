package ru.komaric.birthdayreminder.sql;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public VK Vk;
    public LOCAL Local;

    private Context context;
    private File root;
    private static FileManager instance;

    private FileManager(Context context) {
        this.context = context.getApplicationContext();
        root = context.getFilesDir();
        Vk = new VK();
        Local = new LOCAL();
    }

    public static void initialize(Context context) {
        if (instance == null) {
            synchronized (FileManager.class) {
                if (instance == null) {
                    instance = new FileManager(context);
                }
            }
        }
    }

    public static FileManager getFileManager() {
        if (instance == null) {
            throw new NullPointerException("FileManager is not initialized");
        }
        return instance;
    }

    public final class LOCAL {

    }

    public final class VK {

        private final static String dirName = "vk";
        private final static String EMPTY_IMAGE = "camera_50.png";

        private VK() {
            File dir = new File(root, dirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        public void deleteAll() {
            File dir = new File(root, dirName);
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }

        public void delete(List<String> imageNames) {
            for (String imageName : imageNames) {
                (new File(root, dirName + File.separator + imageName)).delete();
            }
        }

        public boolean isEmptyImage(String sUrl) {
            return sUrl.substring(sUrl.lastIndexOf('/') + 1).equals(EMPTY_IMAGE);
        }

        public File getImageFileIfExists(String sUrl) {
            String filename = sUrl.substring(sUrl.lastIndexOf('/') + 1);
            File file = new File(root, dirName + File.separator + filename);
            return file.exists() ? file : null;
        }

        public File download(String sUrl) {
            try {
                if (isEmptyImage(sUrl)) {
                    return null;
                }
                URL url = new URL(sUrl);
                String filename = sUrl.substring(sUrl.lastIndexOf('/') + 1);
                File file = new File(root, dirName + File.separator + filename);
                if (!file.exists()) {
                    file.createNewFile();
                }
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(file);
                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) >= 0) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public List<String> getFileNamesList() {
            File[] files = (new File(root, dirName)).listFiles();
            List<String> fileNames = new ArrayList<>(files.length);
            for (File file : files) {
                fileNames.add(file.getName());
            }
            return fileNames;
        }

    }
}
