import controller.menu.MainMenuController;
import model.menu.MusicManager;
import view.menu.MainMenuView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize Model
            MusicManager musicManager = new MusicManager();
            musicManager.loadAndPlay("resources/background.wav");

            // Initialize View
            MainMenuView view = new MainMenuView();

            // Initialize Controller (binds view + model)
            MainMenuController controller = new MainMenuController(view, musicManager);

            // Show the UI
            controller.show();
        });
    }
}
