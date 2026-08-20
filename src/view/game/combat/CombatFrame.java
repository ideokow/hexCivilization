package view.game.combat;

import javax.swing.*;

public class CombatFrame extends JFrame {

    public CombatFrame(CombatPanel combatPanel) {
        super("H2H Combat");
        // Combat spends AP at start; close only through the controller's Done action.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(combatPanel);
        setSize(760, 460);
        setLocationRelativeTo(null);
    }
}
