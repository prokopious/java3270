package j3270;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Emulator3270Runner implements Runnable {

  private int scriptPort;
  private boolean visible = false;
  private boolean nvt = false;
  private String model = "3279-2";

  private Process process = null;
  private boolean started = false;

  public Emulator3270Runner(int scriptPort) {
    super();
    this.scriptPort = scriptPort;
  }

  @Override
  public void run() {

    ProcessBuilder pb = new ProcessBuilder("s3270", "-scriptport", "[::1]:"+scriptPort+"", "-model", "3279-2");
    try {
      process = pb.start();
      started = true;
      process.waitFor();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
   
    } finally {
      stopNow();
    }

  }


  public void stopNow() {
    if (process != null && process.isAlive()) {
    
      process.destroyForcibly();
    }
  }

  public void stop() {
    if (process != null && process.isAlive()) {
      process.destroy();
    }
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isNvt() {
    return nvt;
  }

  public void setNvt(boolean nvt) {
    this.nvt = nvt;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

}
