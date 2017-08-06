package net.acomputerdog.picam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.acomputerdog.picam.camera.Camera;
import net.acomputerdog.picam.config.PiConfig;
import net.acomputerdog.picam.gpio.GpioManager;
import net.acomputerdog.picam.system.FileSystem;
import net.acomputerdog.picam.system.net.Network;
import net.acomputerdog.picam.web.WebServer;

import java.io.*;

public class PiCamController {

    private final File cfgFile;
    private final Gson gson;

    private final Thread shutdownHandler;

    private WebServer webServer;
    private Camera[] cameras;

    private PiConfig config;

    private FileSystem fileSystem;
    private Network network;

    private File baseDir;
    private File vidDir;
    private File picDir;
    private File streamDir;
    private File tmpDir;

    private GpioManager gpio;

    private PiCamController(String configPath) {
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            this.gson = builder.create();

            this.cfgFile = new File(configPath);

            if (!cfgFile.exists()) {
                createDefaultConfig();
            }
            try (JsonReader reader = new JsonReader(new FileReader(cfgFile))) {
                this.config = gson.fromJson(reader, PiConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("IO error while loading config", e);
            }

            readConfig();

            // shutdown handler to take care of exit tasks
            this.shutdownHandler = new Thread(this::shutdown);
            Runtime.getRuntime().addShutdownHook(shutdownHandler);
        } catch (Exception e) {
            throw new RuntimeException("Exception setting up camera", e);
        }
    }

    private void start() {
        webServer.start();
        System.out.println("Started.");
    }

    public void exit() {
        // don't cause shutdown to be called twice
        Runtime.getRuntime().removeShutdownHook(shutdownHandler);
        shutdown();
        System.exit(0);
    }

    private void shutdown() {
        try {
            System.out.println("Shutting down controller.");
            saveConfig();
            gpio.shutdown();
        } catch (Exception e) {
            System.err.println("Exception while shutting down");
            e.printStackTrace();
        }
    }

    private void readConfig() {
        try {
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

            this.fileSystem = new FileSystem(config);
            this.network = new Network(this);

            // we can't restart the web server without causing the program to exit
            if (this.webServer == null) {
                this.webServer = new WebServer(this);
            }

            if (cameras != null) {
                for (Camera camera : cameras) {
                    if (camera.isRecording()) {
                        camera.stop();
                    }
                }
            }
            this.cameras = new Camera[]{new Camera(this, 0)};

            if (gpio != null) {
                gpio.shutdown();
            }
            gpio = new GpioManager(this);
        } catch (Exception e) {
            throw new RuntimeException("Exception initializing components", e);
        }
    }

    public void saveConfig() throws IOException {
        try (Writer writer = new FileWriter(cfgFile)) {
            gson.toJson(config, writer);
        }
    }

    private void createDefaultConfig() throws IOException {
        resetConfig();
        saveConfig();
    }

    public void resetConfig() {
        config = PiConfig.createDefault();
    }

    public void updateConfig(JsonObject json) {
        config = gson.fromJson(json, PiConfig.class);
        readConfig();
    }

    public void updateConfig(String json) {
        config = gson.fromJson(json, PiConfig.class);
        readConfig();
    }

    public void clearCache() throws IOException {
        File[] tmpContents = getTmpDir().listFiles();
        if (tmpContents != null && tmpContents.length > 0) {
            for (File f : tmpContents) {
                if (!f.delete()) {
                    System.err.printf("Warning: unable to delete file '%s'\n", f.getPath());
                }
            }
        }

        File[] streamContents = getStreamDir().listFiles();
        if (streamContents != null && streamContents.length > 0) {
            for (File f : streamContents) {
                if (!f.delete()) {
                    System.err.printf("Warning: unable to delete file '%s'\n", f.getPath());
                }
            }
        }
    }

    public Camera getCamera(int num) {
        return cameras[num];
    }

    public JsonObject getConfigJson() {
        return (JsonObject)gson.toJsonTree(config);
    }

    public String getVersionString() {
        return "Pi Camera Controller v0.4.0";
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

    public FileSystem getFS() {
        return fileSystem;
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) {
        String path = "camera.cfg";
        if (args.length > 0) {
            path = args[0];
        }
        new PiCamController(path).start();
    }
}
