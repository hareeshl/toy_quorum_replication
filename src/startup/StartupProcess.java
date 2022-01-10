package startup;

// This interface defines behavior to be implemented for all functionality that needs to be part of node bootup
public interface StartupProcess {
    void start();
    void stop();
}
