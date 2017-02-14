package com.deaddropgames.editor.gui;

import com.badlogic.gdx.utils.JsonValue;
import com.deaddropgames.editor.pickle.ApiBaseList;
import com.deaddropgames.editor.pickle.Level;
import com.deaddropgames.editor.web.LevelRepository;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DownloadLevelDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable levelTable;
    private LevelListTableModel levelListTableModel;
    private JButton previousBtn;
    private JButton nextBtn;
    private JLabel statusLabel;
    private JTextField searchTextField;
    private JComboBox<String> difficultyComboBox;
    private JCheckBox showMineCheckbox;
    private JButton searchButton;

    private LevelRepository levelRepository;
    private ApiBaseList levelList;
    private Level level;
    private int page;

    public DownloadLevelDialog(LevelRepository levelRepository) {

        this.levelRepository = levelRepository;

        statusLabel.setText("");
        level = null;

        setBounds(100, 100, 600, 500);
        setContentPane(contentPane);
        setModal(true);
        setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.title"));
        levelListTableModel = new LevelListTableModel();
        levelTable.setModel(levelListTableModel);
        levelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getRootPane().setDefaultButton(buttonOK);

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

        previousBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                onPrevious();
            }
        });
        nextBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                onNext();
            }
        });

        difficultyComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.comboDifficulty.all"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.circle"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.square"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.diamond"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.doubleDiamond")
        }));

        difficultyComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                onDifficultyChange();
            }
        });

        showMineCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                onShowMine();
            }
        });

        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                onSearchBtn();
            }
        });

        page = 1;
        loadLevelList(null);
    }

    @Override
    public void setVisible(boolean b) {

        loadLevelList(null);
        super.setVisible(b);
    }

    private void onSearchBtn() {

        loadLevelList(null);
    }

    private void onShowMine() {

        // have to be logged in for this one
        if (showMineCheckbox.isSelected() && !levelRepository.hasToken()) {

            if (!initializeUserToken()) {

                showMineCheckbox.setSelected(false);
                return;
            }
        }

        loadLevelList(null);
    }

    private void onDifficultyChange() {

        loadLevelList(null);
    }

    public Level getLevel() {

        return level;
    }

    private void onOK() {

        level = null;
        int row = levelTable.getSelectedRow();
        if (row < 0) {

            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.noRowSelected.text"),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.noRowSelected.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        long id = levelListTableModel.getSelectedId(row);
        try {

            level = levelRepository.getLevel(id);

            // since we don't send the ID in the body for a PUT, we need to set it manually (since it's transient)
            level.setId(id);
        } catch (IOException | URISyntaxException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.failedLevelLoad.title"),
                    JOptionPane.ERROR_MESSAGE);
        }

        setVisible(false);
    }

    private void onCancel() {

        level = null;
        setVisible(false);
    }

    private void onPrevious() {

        page--;
        loadLevelList(levelList.getPrevious());
    }

    private void onNext() {

        page++;
        loadLevelList(levelList.getNext());
    }

    private List<NameValuePair> getQueryParams() {

        List<NameValuePair> nameValuePairList = new ArrayList<>();

        // 0 is all, so we can ignore it
        if (difficultyComboBox.getSelectedIndex() > 0) {

            nameValuePairList.add(new BasicNameValuePair("difficulty",
                    String.valueOf(difficultyComboBox.getSelectedIndex() - 1)));
        }

        if (!searchTextField.getText().isEmpty()) {

            nameValuePairList.add(new BasicNameValuePair("search", searchTextField.getText()));
        }

        return nameValuePairList;
    }

    private void loadLevelList(String path) {

        try {

            levelList = levelRepository.getLevelList(path, getQueryParams(), showMineCheckbox.isSelected());
        } catch (IOException | URISyntaxException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.failedListLoad.title"),
                    JOptionPane.ERROR_MESSAGE);
        }

        statusLabel.setText(String.format(
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.results.label"),
                levelList.getCount(), page));

        previousBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        if (levelList.getPrevious() != null && !levelList.getPrevious().isEmpty()) {

            previousBtn.setEnabled(true);
        }

        if (levelList.getNext() != null && !levelList.getNext().isEmpty()) {

            nextBtn.setEnabled(true);
        }

        levelListTableModel.updateTable(levelList);
    }

    private boolean initializeUserToken() {

        LoginDialog loginDialog = new LoginDialog(levelRepository);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setVisible(true);

        return !loginDialog.wasCancelled();
    }

    private class LevelListTableModel extends AbstractTableModel {

        private String[] columnNames = {
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col1Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col2Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col3Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col4Name")
        };

        private String[] difficultyNames = {
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.circle"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.square"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.diamond"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.doubleDiamond")
        };

        private Object[][] data = {};
        private long[] id = {};

        void updateTable(final ApiBaseList levelList) {

            int numResults = levelList.getResults().size;
            data = new Object[numResults][4];
            id = new long[numResults];
            for (int ii = 0; ii < numResults; ii++) {

                JsonValue value = (JsonValue)levelList.getResults().get(ii);

                id[ii] = value.get("id").asLong();
                data[ii][0] = value.get("name").asString();
                data[ii][1] = value.get("author").asString();
                int difficulty = value.get("difficulty").asInt();
                data[ii][2] = difficulty < difficultyNames.length ? difficultyNames[difficulty] : "?";
                data[ii][3] = value.get("description").asString();
            }

            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {

            return data.length;
        }

        @Override
        public int getColumnCount() {

            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            return data[rowIndex][columnIndex];
        }

        @Override
        public String getColumnName(int columnIndex) {

            return columnNames[columnIndex];
        }

        long getSelectedId(int rowIndex) {

            return id[rowIndex];
        }
    }
}
