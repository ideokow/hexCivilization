package view.menu;

import javax.swing.*;
import java.awt.*;

public class LoadingFrame extends JFrame {

    public LoadingFrame() {
        super("Loading");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel label = new JLabel("Loading Game ...", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));

        add(label);
    }

    public void showLoading() {
        setVisible(true);
    }

    public void closeLoading() {
        dispose();
    }
}