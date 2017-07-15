package net.acomputerdog.picam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.acomputerdog.picam.camera.Camera;
import net.acomputerdog.picam.config.PiConfig;
import net.acomputerdog.picam.system.net.Network;
import net.acomputerdog.picam.web.WebServer;

import java.io.*;

public class PiCamController {

    private final WebServer webServer;
    private final Camera[] cameras;

    private final File cfgFile;
    private final Gson gson;
    private final PiConfig config;

    private final Network network;

    private final File baseDir;
    private final File vidDir;
    private final File picDir;
    private final File streamDir;
    private final File tmpDir;

    private PiCamController() {
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            this.gson = builder.create();

            this.cfgFile = new File("camera.cfg");

            if (!cfgFile.exists()) {
                createDefaultConfig();
            }
            try (JsonReader reader = new JsonReader(new FileReader(cfgFile))) {
                this.config = gson.fromJson(reader, PiConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("IO error while loading config", e);
            }

            this.network = new Network(this);

            this.webServer = new WebServer(this);
            this.cameras = new Camera[]{new Camera(this, 0)};

            baseDir = new File(config.baseDirectory);
            if (!baseDir.isDirectory() && !baseDir.mkdir()) {
                System.out.println("Unable to create base directory");
            }

            vidDir = new File(baseDir, "videos/");
            if (!vidDir.isDirectory() && !vidDir.mkdir()) {
                System.out.println("Unable to create video directory");
            }

            picDir = new File(baseDir, "snapshots/");
            if (!picDir.isDirectory() && !picDir.mkdir()) {
                System.out.println("Unable to create snapshot directory");
            }

            streamDir = new File(baseDir, "stream/");
            if (!streamDir.isDirectory() && !streamDir.mkdir()) {
                System.out.println("Unable to create stream directory");
            }

            tmpDir = new File(baseDir, "tmp/");
            if (!tmpDir.isDirectory() && !tmpDir.mkdir()) {
                System.out.println("Unable to create temp directory");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception setting up camera", e);
        }
    }

    private void start() {
        webServer.start();
        System.out.println("Started.");
    }

    public void shutdown() {
        try {
            System.out.println("Shutting down controller.");
            saveConfig();
        } catch (Exception e) {
            System.err.println("Exception while shutting down");
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void saveConfig() throws IOException {
        try (Writer writer = new FileWriter(cfgFile)) {
            gson.toJson(config, writer);
        }
    }

    private void createDefaultConfig() throws IOException {
        PiConfig def = PiConfig.createDefault();
        try (Writer writer = new FileWriter(cfgFile)) {
            gson.toJson(def, writer);
        }
    }

    public Camera getCamera(int num) {
        return cameras[num];
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getVidDir() {
        return vidDir;
    }

    public File getPicDir() {
        return picDir;
    }

    public File getStreamDir() {
        return streamDir;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public PiConfig getConfig() {
        return config;
    }

    public Network getNetwork() {
        return network;
    }

    public static void main(String[] args) {
        new PiCamController().start();
    }
}
