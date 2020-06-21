package in.hauu.gui;

import javax.swing.*;

public class GuiManager implements Runnable {

    public static DiaryExporter diaryExporter = new DiaryExporter();

    public static void popUp(String results) {
        JOptionPane.showMessageDialog(diaryExporter.getPanel(), results);
    }

    @Override
    public void run() {
        provideListener(
                diaryExporter.getПоехалиКнопка(),
                diaryExporter.getLoginField(),
                diaryExporter.getPasswordField()
        );
    }

    protected void provideListener(
            JButton start,
            JTextField textField,
            JPasswordField passwordField
    ) {
        start.addActionListener(new ParamListerner(
                new ParamHolder(
                        textField,
                        passwordField
                )
        ));

    }
}
