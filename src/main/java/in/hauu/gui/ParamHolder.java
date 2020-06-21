package in.hauu.gui;

import javax.swing.*;

public class ParamHolder {

    private final JTextField textField;
    private final JPasswordField passwordField;
    // DiaryService diaryService = DiaryService.DARKDIARY;

    public ParamHolder(JTextField textField, JPasswordField passwordField) {
        this.textField = textField;
        this.passwordField = passwordField;
    }

    public String getLogin() {
        return textField.getText().trim();
    }

    public String getPassword() {
        // httpclient will not accept char[] anyway, so we'll use this less secure method
        return new String(passwordField.getPassword());
    }
}
