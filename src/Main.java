import java.awt.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.NativeHookException;

public class Main {
    public static void main(String[] args) {
        CapsLockNotifier.main(args);
    }
}

class CapsLockNotifier implements NativeKeyListener {

    private JWindow overlayWindow;
    private JLabel imageLabel;
    private boolean isCapsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
    private ScheduledExecutorService scheduler;

    private static final String BASE64_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAIAAAAiOjnJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAPrSURBVHhe7dJZQlsxFARR9r8tNhYCRcLgiaGvn9R1/hLAbkn18CgFGJYiDEsRhqUIw1KEYSnCsBRhWIowLEUYliIMSxGGpQjDUoRhKcKwFGFYijAsRRiWIgxLEYalCMNShGEpwrAUYViKMCxFGJYiDEsRhqUIw1KEYSnCsBRhWIowLEUYliIMSxGGpQjDUoRhKcKwFGFYijAsRRiWIgzrVg/P+Ieu8aZu8lLVC/5LF3lN1xHUG/xA53lHV5DSJ/xYZ3hBlxDRGfySTvF2LqGgM/glneLtnEU+F/Gr+sSrOY1wbsAf6D3v5QSSuRl/pje8lI+I5Yv4Y73yRj6ilC/ij/XKG3mHTL6Fj9Azr+M/AvkBPkiG9Q9p/BgfV8+LAF38GB9Xz4v4iyh+CR/azVv45ape8NHF2q+AEAL4glbV5yeBGL6mkmEF8TWVeg/P44fxZX1KT86zj+AryzQemwcfxBc3MawJfHGTujPz1OP4+hpdB+aR74QRHYpOy/PeFVMKtByVhz0ABu3OsKYxaHcV5+RJD4NZW9v/kDzmwTBuX5ufkGc8JCZuyrDuhomb2vl4POCBMXRH256Npzs85m5nz4PxaItg9F42PBXPtRSmb8SwDoHpG9ntSDzUgjjALrY6D0+0LI6xhX0Ow+MsjsOsz7COhcOsb5OT8Cxb4EiL2+EYPMhGONjKlj8DT7EdjrcswzouTrimxddvjUOuaeH1XP/WOOqCVp3OxRfgwKsxrKPjwKsxrKPjwKtZdfcTLn5rHHVBC0//Bp7rThjRwbDmMKKDYc1hRAfDmsOIDoY1hxEdDGsOIzoY1hxGdDCsOYzoYFhzGNHBsOYwooNhzWFEB8Oaw4gOhjWHER0Maw4jOhjWHEZ0MKw5jOhgWHMY0cGw5jCig2HNYUQHw5rDiA6GNYcRHQxrDiM6GNYcRnQwrDmM6GBYcxjRwbDmMKKDYc1hRAfDmsOIDoY1hxEdDGsOIzoY1hxGdDCsOYzoYFhzGNHBsOYwooNhzWFEB8Oaw4gOhjWHER0Maw4jOhjWHEZ0MKw5jOhgWHMY0cGw5jCig2HNYUQHw5rDiA6GNYcRHQxrDiM6dJ32CY88jq+vUXdgzTAsRRiWIgxLEYalCMNShGEpwrAUYViKMCxFGJYiDEsRhqUIw1KEYSnCsBRhWIowLEUYliIMSxGGpQjDUoRhKcKwFGFYijAsRRiWIgxLEYalCMNShGEpwrAUYViKMCxFGJYiDEsRhqUIw1KEYSnCsBRhWIowLEUYliIMSxGGpQjDUoRhKcKwFGFYijAsRRiWIgxLEYalCMNShGEpwrAUYViKMCxFGJYCHh//AGQAyiPwMzXGAAAAAElFTkSuQmCC";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                CapsLockNotifier notifier = new CapsLockNotifier();
                notifier.createAndShowGUI();
                notifier.checkCapsLockStatus();
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
        });
    }

    public void checkCapsLockStatus() {
        boolean isCapsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        System.out.println("Caps Lock Status on Initialization: " + (isCapsLockOn ? "Active" : "Inactive"));
        this.isCapsLockOn = isCapsLockOn;
    }

    public void createAndShowGUI() {
        // Create the overlay window
        overlayWindow = new JWindow();
        overlayWindow.setBackground(new Color(0, 0, 0, 0)); // Transparent background

        // Load the Base64 image and set it on a JLabel
        ImageIcon icon = new ImageIcon(decodeBase64Image(BASE64_IMAGE));
        imageLabel = new JLabel(icon);
        overlayWindow.getContentPane().add(imageLabel);

        // Set the window size to match the image size
        overlayWindow.pack();

        // Position the window at the center of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - overlayWindow.getWidth()) / 2;
        int y = (screenSize.height - overlayWindow.getHeight()) / 2;
        overlayWindow.setLocation(x, y);

        // Add this instance as a key listener
        GlobalScreen.addNativeKeyListener(this);

        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {}

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Check Caps Lock status on key release
        if (e.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
            handleCapsLockChange();
        }
    }

    private void handleCapsLockChange() {
        this.isCapsLockOn = !this.isCapsLockOn;
        System.out.println("Caps Lock State: " + isCapsLockOn);
        if (this.isCapsLockOn) {
            showOverlay();
        } else {
            hideOverlay();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    private void showOverlay() {
        overlayWindow.setVisible(true);
        scheduler.schedule(this::hideOverlay, 1, TimeUnit.SECONDS);
    }

    private void hideOverlay() {
        overlayWindow.setVisible(false);
    }

    private Image decodeBase64Image(String base64String) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}