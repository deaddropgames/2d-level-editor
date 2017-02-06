package com.deaddropgames.editor.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public class LoginDialog extends JDialog implements ActionListener {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton okButton;
    private JButton cancelButton;
    private JButton linkButton;
    private boolean wasCancelled;

    public LoginDialog() {

        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.loginDlg.title"));
        setBounds(100, 100, 450, 200);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblUsername = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.txtUsername.text"));
            GridBagConstraints gbc_lblUsername = new GridBagConstraints();
            gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
            gbc_lblUsername.anchor = GridBagConstraints.EAST;
            gbc_lblUsername.gridx = 0;
            gbc_lblUsername.gridy = 0;
            contentPanel.add(lblUsername, gbc_lblUsername);
        }
        {
            txtUsername = new JTextField();
            GridBagConstraints gbc_txtUsername = new GridBagConstraints();
            gbc_txtUsername.insets = new Insets(0, 0, 5, 0);
            gbc_txtUsername.fill = GridBagConstraints.WEST;
            gbc_txtUsername.gridx = 1;
            gbc_txtUsername.gridy = 0;
            contentPanel.add(txtUsername, gbc_txtUsername);
            txtUsername.setColumns(10);
        }
        {
            JLabel lblPassword = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.txtPassword.text"));
            GridBagConstraints gbc_lblPassword = new GridBagConstraints();
            gbc_lblPassword.anchor = GridBagConstraints.EAST;
            gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
            gbc_lblPassword.gridx = 0;
            gbc_lblPassword.gridy = 1;
            contentPanel.add(lblPassword, gbc_lblPassword);
        }
        {
            txtPassword = new JPasswordField();
            GridBagConstraints gbc_txtPassword = new GridBagConstraints();
            gbc_txtPassword.insets = new Insets(0, 0, 5, 0);
            gbc_txtPassword.fill = GridBagConstraints.WEST;
            gbc_txtPassword.gridx = 1;
            gbc_txtPassword.gridy = 1;
            contentPanel.add(txtPassword, gbc_txtPassword);
            txtPassword.setColumns(10);
        }

        // only add a link if we can actually click it...
        if (Desktop.isDesktopSupported()) {

            {
                JLabel lblRegister = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.txtRegister.text"));
                GridBagConstraints gbc_lblRegister = new GridBagConstraints();
                gbc_lblRegister.insets = new Insets(0, 0, 5, 5);
                gbc_lblRegister.anchor = GridBagConstraints.EAST;
                gbc_lblRegister.gridx = 0;
                gbc_lblRegister.gridy = 2;
                contentPanel.add(lblRegister, gbc_lblRegister);
            }
            {
                linkButton = new JButton(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.txtRegisterLink.text"));
                linkButton.addActionListener(this);
                GridBagConstraints gbc_linkRegister = new GridBagConstraints();
                gbc_linkRegister.insets = new Insets(0, 0, 5, 5);
                gbc_linkRegister.anchor = GridBagConstraints.WEST;
                gbc_linkRegister.gridx = 1;
                gbc_linkRegister.gridy = 2;
                contentPanel.add(linkButton, gbc_linkRegister);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                okButton = new JButton(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.okButton.text"));
                okButton.addActionListener(this);
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                cancelButton = new JButton(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.cancelButton.text"));
                cancelButton.addActionListener(this);
                buttonPane.add(cancelButton);
            }
        }

        wasCancelled = true;
    }

    public boolean wasCancelled() {

        return wasCancelled;
    }

    public String getUsername() {

        return txtUsername.getText();
    }

    public char[] getPassword() {

        return txtPassword.getPassword();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == okButton) {

            String errMsg = "";
            if(txtUsername.getText().isEmpty()) {

                errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.validationError.username") + "\n";
            }

            if(txtPassword.getPassword().length == 0) {

                errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LoginDialog.validationError.password") + "\n";
            }

            if(!errMsg.isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        errMsg,
                        ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.dlgTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // set cancelled flag to false
            wasCancelled = false;
        } else if(e.getSource() == cancelButton) {

            wasCancelled = true;
        } else if(e.getSource() == linkButton) {

            if (Desktop.isDesktopSupported()) {

                try {

                    Desktop.getDesktop().browse(new URI("http://deaddropgames.com/"));
                } catch (IOException | URISyntaxException e1) {

                    e1.printStackTrace();
                }
            }
        }

        // hide the dialog
        setVisible(false);
    }
}
