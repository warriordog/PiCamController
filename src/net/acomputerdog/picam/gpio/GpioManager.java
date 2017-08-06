package net.acomputerdog.picam.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;
import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.file.H264File;
import net.acomputerdog.picam.system.Power;

public class GpioManager {
    private final PiCamController controller;

    private final GpioController gpio;
    private final GpioPinDigitalInput recordToggle;
    private final GpioPinDigitalInput shutdownButton;

    public GpioManager(PiCamController controller) {
        this.controller = controller;

        //enable non-root GPIO
        GpioUtil.enableNonPrivilegedAccess();

        this.gpio = GpioFactory.getInstance();

        this.recordToggle = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, PinPullResistance.PULL_UP);
        this.recordToggle.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        this.recordToggle.setDebounce(100);
        this.recordToggle.addListener((GpioPinListenerDigital) event -> {
            if (event.getEdge() == PinEdge.RISING) {
                if (controller.getCamera(0).isRecording()) {
                    controller.getCamera(0).stop();
                } else {
                    controller.getCamera(0).recordFor(0, new H264File(controller.getVidDir(), "toggled_video"));
                }
            }
        });

        this.shutdownButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_UP);
        this.shutdownButton.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        this.shutdownButton.setDebounce(100);
        this.shutdownButton.addListener((GpioPinListenerDigital) event -> {
            if (event.getEdge() == PinEdge.RISING) {
                Power.shutdown();
                controller.exit();
            }
        });
    }

    public void shutdown() {
        gpio.unprovisionPin(recordToggle);
        gpio.unprovisionPin(shutdownButton);
        gpio.shutdown();
    }
}
