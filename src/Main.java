import java.awt.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.NativeHookException;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        CapsLockNotifier.main(args);
    }
}

class CapsLockNotifier implements NativeKeyListener {
    private static final Logger logger = LoggerFactory.getLogger(GlobalScreen.class);
    private JWindow overlayWindow;
    private boolean isCapsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
    private ScheduledExecutorService scheduler;
    private static final String BASE64_IMAGE = "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";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                CapsLockNotifier notifier = new CapsLockNotifier();
                notifier.createAndShowGUI();
                notifier.checkCapsLockStatus();
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException e) {
                logger.error("Error when trying to register native hook.", e);
            }
        });
    }

    public void checkCapsLockStatus() {
        boolean isCapsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        System.out.println("Caps Lock Status on Initialization: " + (isCapsLockOn ? "Active" : "Inactive"));
        this.isCapsLockOn = isCapsLockOn;
    }

    public void createAndShowGUI() {
        overlayWindow = new JWindow();
        overlayWindow.setAlwaysOnTop(true);
        overlayWindow.setBackground(new Color(0, 0, 0, 0)); // Transparent background

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(decodeBase64Image(), "Image must not be null"));
        JLabel imageLabel = new JLabel(icon);
        overlayWindow.getContentPane().add(imageLabel);
        overlayWindow.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - overlayWindow.getWidth()) / 2;
        int y = (screenSize.height - overlayWindow.getHeight()) / 2;
        overlayWindow.setLocation(x, y);

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

    private Image decodeBase64Image() {

        byte[] imageBytes = Base64.getDecoder().decode(CapsLockNotifier.BASE64_IMAGE);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);

        try {
            return ImageIO.read(bis);
        } catch (IOException e) {
            logger.error("Error when trying to read the base64 ByteArrayInputStream as image.", e);
            return null;
        }
    }
}