package in.hauu;

import in.hauu.gui.GuiManager;

import javax.swing.*;

class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new GuiManager());
    }
}
