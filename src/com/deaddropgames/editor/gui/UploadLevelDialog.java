package com.deaddropgames.editor.gui;

import com.badlogic.gdx.net.HttpStatus;
import com.deaddropgames.editor.pickle.ExportLevel;
import com.deaddropgames.editor.pickle.Level;
import com.deaddropgames.editor.pickle.UploadLevel;
import com.deaddropgames.editor.web.LevelRepository;
import org.apache.http.StatusLine;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public class UploadLevelDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField titleTextField;
    private JComboBox<String> difficultyComboBox;
    private JTextArea descriptionTextArea;
    private JCheckBox createNewCheckBox;
    private LevelRepository levelRepository;
    private ExportLevel exportLevel;
    private Level level;
    private boolean wasCancelled;

    public UploadLevelDialog(final LevelRepository levelRepository, final ExportLevel exportLevel, Level level) {

        this.levelRepository = levelRepository;
        this.exportLevel = exportLevel;
        this.level = level;
        wasCancelled = false;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setBounds(100, 100, 450, 300);

        setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.title"));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        difficultyComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.circle"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.square"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.diamond"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.doubleDiamond")
        }));

        titleTextField.setText(level.getName());
        descriptionTextArea.setText(level.getDescription());
        if (level.getDifficulty() > 0 && level.getDifficulty() < difficultyComboBox.getItemCount()) {

            difficultyComboBox.setSelectedIndex(level.getDifficulty());
        } else {

            difficultyComboBox.setSelectedIndex(0);
        }

        // only set the checkbox visible if we have modified a level that was downloaded
        createNewCheckBox.setSelected(false);
        if (level.getId() <= 0) {

            createNewCheckBox.setVisible(false);
        } else {

            createNewCheckBox.setVisible(true);
        }
    }

    private void onOK() {

        wasCancelled = false;

        // make sure the required fields are full
        String errMsg = "";
        if(titleTextField.getText().isEmpty()) {

            errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.title") + "\n";
        }

        if(descriptionTextArea.getText().isEmpty()) {

            errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.desc") + "\n";
        }

        if(level.getEndX() < 0f) {

            errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.badEndX.text") + "\n";
        }

        if(!errMsg.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    errMsg,
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.validationError.dlgTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create a new upload level and assign data
        UploadLevel uploadLevel = new UploadLevel(exportLevel, level);
        uploadLevel.setName(titleTextField.getText());
        uploadLevel.setDifficulty(difficultyComboBox.getSelectedIndex());
        uploadLevel.setDescription(descriptionTextArea.getText());

        // if the checkbox is selected or there is no ID for the level, it means we should create a new level (POST)
        // otherwise we are updating (PUT)
        long id = level.getId();
        try {

            if (createNewCheckBox.isSelected() || id <= 0) {

                id = levelRepository.createLevel(uploadLevel);
            } else {

                id = levelRepository.modifyLevel(id, uploadLevel);
            }
        } catch (IOException | URISyntaxException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.failedLevelUpload.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // the id should always be greater than 0!
        if (id <= 0) {

            String errMessage = String.format(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.failedLevelUpload.text"),
                    levelRepository.getStatusLine().getStatusCode(), levelRepository.getStatusLine().getReasonPhrase());

            // special case when error is 403 Forbidden - means user doesn't own this resource so they can't change it
            StatusLine statusLine = levelRepository.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_FORBIDDEN) {

                errMessage = ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages"). getString("UploadLevelDialog.forbiddenUpload.text");
            }

            JOptionPane.showMessageDialog(this,
                    errMessage,
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("UploadLevelDialog.failedLevelUpload.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // update the level's ID with the response ID (in case it was a new level)
        level.setId(id);

        setVisible(false);
    }

    private void onCancel() {

        wasCancelled = true;
        setVisible(false);
    }

    public boolean wasCancelled() {

        return wasCancelled;
    }

    public String getTitle() {

        return titleTextField.getText();
    }
}
