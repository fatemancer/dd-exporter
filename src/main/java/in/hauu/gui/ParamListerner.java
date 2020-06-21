package in.hauu.gui;

import in.hauu.Launcher;
import lombok.AllArgsConstructor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@AllArgsConstructor
public class ParamListerner implements ActionListener {

    private ParamHolder paramHolder;

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] args = { paramHolder.getLogin(), paramHolder.getPassword(), "true" };
        Launcher.core(args);
    }
}
