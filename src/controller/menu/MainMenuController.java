package controller.menu;

import model.menu.MusicManager;
import view.menu.MainMenuView;

import javax.swing.*;

/*
 * main menu controller
 */
public class MainMenuController {

    private final MainMenuView view;
    private final MusicManager musicManager;

    public MainMenuController(MainMenuView view, MusicManager musicManager) {
        this.view = view;
        this.musicManager = musicManager;

        attachListeners();
    }

    private void attachListeners() {
        // Start button: launch the game
        view.getStartButton().addActionListener(e -> handleStart());

        // Settings button: open settings dialog
        view.getSettingsButton().addActionListener(e -> handleSettings());

        // Exit button: show confirmation and quit
        view.getExitButton().addActionListener(e -> handleExit());

        // Window close button also triggers exit confirmation
        view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleExit();
            }
        });
    }

    private void handleStart() {
        // TODO: start here
    }

    private void handleSettings() {
        SettingsController settingsController = new SettingsController(view, musicManager);
        settingsController.show();
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to exit?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            musicManager.stop();
            view.dispose();
            System.exit(0);
        }
    }

    public void show() {
        view.setVisible(true);
    }
}
