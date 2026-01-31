/**
 * Smart Home Management System
 * -----------------------------------
 * This program simulates a smart home system managing Lights, Cameras, and Heaters.
 * Each device can be turned on/off, has specific attributes, and may support charging or recording.
 * Commands allow controlling devices and viewing their statuses.
 *
 * Devices:
 * - Lights: brightness (LOW, MEDIUM, HIGH), color (YELLOW, WHITE), chargeable
 * - Cameras: angle (-60 to 60), recording, chargeable
 * - Heaters: temperature (15°C to 30°C), non-chargeable
 *
 * Example commands:
 * DisplayAllStatus
 * TurnOn Light 0
 * SetBrightness Light 0 HIGH
 * SetColor Light 0 WHITE
 * StartCharging Camera 4
 * SetTemperature Heater 6 25
 * StartRecording Camera 4
 * end
 *
 * Output follows success/error messages per device rules.
 */

import java.util.*;

public class SmartHomeManagementSystem {
    static final int DEFAULT_CAMERA_ANGLE = 45;
    static final int DEFAULT_HEATER_TEMP = 20;

    public static void main(String[] args) {
        List<SmartDevice> devices = new ArrayList<>();

        // Initialize Lights
        devices.add(new Light(Status.ON, false, BrightnessLevel.LOW, LightColor.YELLOW, 0));
        devices.add(new Light(Status.ON, false, BrightnessLevel.LOW, LightColor.YELLOW, 1));
        devices.add(new Light(Status.ON, false, BrightnessLevel.LOW, LightColor.YELLOW, 2));
        devices.add(new Light(Status.ON, false, BrightnessLevel.LOW, LightColor.YELLOW, 3));

        // Initialize Cameras
        devices.add(new Camera(Status.ON, false, false, DEFAULT_CAMERA_ANGLE, 4));
        devices.add(new Camera(Status.ON, false, false, DEFAULT_CAMERA_ANGLE, 5));

        // Initialize Heaters
        devices.add(new Heater(Status.ON, DEFAULT_HEATER_TEMP, 6));
        devices.add(new Heater(Status.ON, DEFAULT_HEATER_TEMP, 7));
        devices.add(new Heater(Status.ON, DEFAULT_HEATER_TEMP, 8));
        devices.add(new Heater(Status.ON, DEFAULT_HEATER_TEMP, 9));

        Scanner scanner = new Scanner(System.in);
        Set<String> validCommands = new HashSet<>(Arrays.asList(
                "DisplayAllStatus", "TurnOn", "TurnOff", "StartCharging", "StopCharging",
                "SetTemperature", "SetBrightness", "SetColor", "SetAngle", "StartRecording", "StopRecording"));

        while (scanner.hasNextLine()) {
            String commandLine = scanner.nextLine().trim();
            if (commandLine.equals("end")) break;
            if (commandLine.isEmpty()) continue;

            String[] tokens = commandLine.split("\\s+");
            String cmd = tokens[0];
            if (!validCommands.contains(cmd)) {
                System.out.println("Invalid command");
                continue;
            }

            if (cmd.equals("DisplayAllStatus")) {
                if (tokens.length != 1) { System.out.println("Invalid command"); continue; }
                for (SmartDevice d : devices) System.out.println(d.displayStatus());
                continue;
            }

            if (tokens.length < 3) { System.out.println("Invalid command"); continue; }

            String deviceName = tokens[1];
            int deviceId;
            try { deviceId = Integer.parseInt(tokens[2]); }
            catch (Exception e) { System.out.println("Invalid command"); continue; }

            SmartDevice device = findDevice(devices, deviceName, deviceId);
            if (device == null) { System.out.println("The smart device was not found"); continue; }

            switch (cmd) {
                case "TurnOn":
                    if (device.isOn()) System.out.println(deviceName + " " + deviceId + " is already on");
                    else { device.turnOn(); System.out.println(deviceName + " " + deviceId + " is on"); }
                    break;
                case "TurnOff":
                    if (!device.isOn()) System.out.println(deviceName + " " + deviceId + " is already off");
                    else { device.turnOff(); System.out.println(deviceName + " " + deviceId + " is off"); }
                    break;
                case "StartCharging":
                    if (!(device instanceof Chargeable)) System.out.println(deviceName + " " + deviceId + " is not chargeable");
                    else {
                        Chargeable c = (Chargeable) device;
                        if (c.isCharging()) System.out.println(deviceName + " " + deviceId + " is already charging");
                        else { c.startCharging(); System.out.println(deviceName + " " + deviceId + " is charging"); }
                    }
                    break;
                case "StopCharging":
                    if (!(device instanceof Chargeable)) System.out.println(deviceName + " " + deviceId + " is not chargeable");
                    else {
                        Chargeable c = (Chargeable) device;
                        if (!c.isCharging()) System.out.println(deviceName + " " + deviceId + " is not charging");
                        else { c.stopCharging(); System.out.println(deviceName + " " + deviceId + " stopped charging"); }
                    }
                    break;
                case "SetTemperature":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Heater)) { System.out.println(deviceName + " " + deviceId + " is not a heater"); break; }
                    int temp;
                    try { temp = Integer.parseInt(tokens[3]); }
                    catch (Exception e) { System.out.println("Invalid command"); break; }
                    Heater heater = (Heater) device;
                    if (temp < Heater.MIN_HEATER_TEMP || temp > Heater.MAX_HEATER_TEMP)
                        System.out.println("Heater " + deviceId + " temperature should be in the range [15, 30]");
                    else { heater.setTemperature(temp); System.out.println(deviceName + " " + deviceId + " temperature is set to " + temp); }
                    break;
                case "SetBrightness":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Light)) { System.out.println(deviceName + " " + deviceId + " is not a light"); break; }
                    BrightnessLevel b;
                    try { b = BrightnessLevel.valueOf(tokens[3]); } 
                    catch (Exception e) { System.out.println("The brightness can only be one of \"LOW\", \"MEDIUM\", or \"HIGH\""); break; }
                    Light light = (Light) device; light.setBrightnessLevel(b);
                    System.out.println(deviceName + " " + deviceId + " brightness level is set to " + b);
                    break;
                case "SetColor":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Light)) { System.out.println(deviceName + " " + deviceId + " is not a light"); break; }
                    LightColor color;
                    try { color = LightColor.valueOf(tokens[3]); }
                    catch (Exception e) { System.out.println("The light color can only be \"YELLOW\" or \"WHITE\""); break; }
                    light = (Light) device; light.setLightColor(color);
                    System.out.println(deviceName + " " + deviceId + " color is set to " + color);
                    break;
                case "SetAngle":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Camera)) { System.out.println(deviceName + " " + deviceId + " is not a camera"); break; }
                    int angle; try { angle = Integer.parseInt(tokens[3]); } catch (Exception e) { System.out.println("Invalid command"); break; }
                    Camera cam = (Camera) device;
                    if (angle < Camera.MIN_CAMERA_ANGLE || angle > Camera.MAX_CAMERA_ANGLE) System.out.println("Camera " + deviceId + " angle should be in the range [-60, 60]");
                    else { cam.setCameraAngle(angle); System.out.println(deviceName + " " + deviceId + " angle is set to " + angle); }
                    break;
                case "StartRecording":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Camera)) { System.out.println(deviceName + " " + deviceId + " is not a camera"); break; }
                    cam = (Camera) device;
                    if (cam.isRecording()) System.out.println(deviceName + " " + deviceId + " is already recording");
                    else { cam.startRecording(); System.out.println(deviceName + " " + deviceId + " started recording"); }
                    break;
                case "StopRecording":
                    if (!device.isOn()) { System.out.println("You can't change the status of the " + deviceName + " " + deviceId + " while it is off"); break; }
                    if (!(device instanceof Camera)) { System.out.println(deviceName + " " + deviceId + " is not a camera"); break; }
                    cam = (Camera) device;
                    if (!cam.isRecording()) System.out.println(deviceName + " " + deviceId + " is not recording");
                    else { cam.stopRecording(); System.out.println(deviceName + " " + deviceId + " stopped recording"); }
                    break;
            }
        }
    }

    private static SmartDevice findDevice(List<SmartDevice> devices, String name, int id) {
        for (SmartDevice d : devices) {
            if (d.getDeviceId() == id) {
                if (name.equals("Light") && d instanceof Light) return d;
                if (name.equals("Camera") && d instanceof Camera) return d;
                if (name.equals("Heater") && d instanceof Heater) return d;
            }
        }
        return null;
    }
}

// Enums and Device Classes
enum Status {ON, OFF}
enum LightColor {WHITE, YELLOW}
enum BrightnessLevel {LOW, MEDIUM, HIGH}

interface Controllable { boolean turnOff(); boolean turnOn(); boolean isOn(); }
interface Chargeable { boolean isCharging(); boolean startCharging(); boolean stopCharging(); }

abstract class SmartDevice implements Controllable {
    protected Status status; protected int deviceId;
    public SmartDevice(Status s, int id) { status = s; deviceId = id; }
    public int getDeviceId() { return deviceId; }
    public boolean isOn() { return status == Status.ON; }
    public boolean turnOn() { if (status==Status.ON) return false; status=Status.ON; return true; }
    public boolean turnOff() { if (status==Status.OFF) return false; status=Status.OFF; return true; }
    public abstract String displayStatus();
}

class Heater extends SmartDevice {
    private int temperature; public static final int MAX_HEATER_TEMP=30, MIN_HEATER_TEMP=15;
    public Heater(Status s, int t, int id){ super(s,id); temperature=t; }
    public void setTemperature(int t){ temperature=t; }
    public String displayStatus(){ return "Heater "+deviceId+" is "+status+" and the temperature is "+temperature+"."; }
}

class Camera extends SmartDevice implements Chargeable {
    private boolean charging, recording; private int angle;
    public static final int MAX_CAMERA_ANGLE=60, MIN_CAMERA_ANGLE=-60;
    public Camera(Status s, boolean c, boolean r, int a, int id){ super(s,id); charging=c; recording=r; angle=a; }
    public boolean isCharging(){ return charging; } public boolean startCharging(){ charging=true; return true; }
    public boolean stopCharging(){ charging=false; return true; }
    public void setCameraAngle(int a){ angle=a; } public boolean isRecording(){ return recording; }
    public void startRecording(){ recording=true; } public void stopRecording(){ recording=false; }
    public String displayStatus(){ return "Camera "+deviceId+" is "+status+", the angle is "+angle+", the charging status is "+charging+", and the recording status is "+recording+"."; }
}

class Light extends SmartDevice implements Chargeable {
    private boolean charging; private BrightnessLevel brightnessLevel; private LightColor lightColor;
    public Light(Status s, boolean c, BrightnessLevel b, LightColor lc, int id){ super(s,id); charging=c; brightnessLevel=b; lightColor=lc; }
    public boolean isCharging(){ return charging; } public boolean startCharging(){ charging=true; return true; }
    public boolean stopCharging(){ charging=false; return true; }
    public void setBrightnessLevel(BrightnessLevel b){ brightnessLevel=b; }
    public void setLightColor(LightColor lc){ lightColor=lc; }
    public String displayStatus(){ return "Light "+deviceId+" is "+status+", the color is "+lightColor+", the charging status is "+charging+", and the brightness level is "+brightnessLevel+"."; }
}
