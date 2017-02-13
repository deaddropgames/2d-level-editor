package com.deaddropgames.editor.gui;

import com.badlogic.gdx.utils.JsonValue;
import com.deaddropgames.editor.pickle.ApiBaseList;
import com.deaddropgames.editor.web.LevelRepository;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public class LevelListDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable levelTable;
    private LevelListTableModel levelListTableModel;
    private JButton previousBtn;
    private JButton nextBtn;
    private JLabel statusLabel;

    private LevelRepository levelRepository;
    private ApiBaseList levelList;

    public LevelListDialog(LevelRepository levelRepository) {

        this.levelRepository = levelRepository;

        statusLabel.setText("");

        setBounds(100, 100, 600, 500);
        setContentPane(contentPane);
        setModal(true);
        setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.title"));
        levelListTableModel = new LevelListTableModel();
        levelTable.setModel(levelListTableModel);

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

        loadLevelList(null);
    }

    private void onOK() {

        // TODO: load the level and return it to the editor
        dispose();
    }

    private void onCancel() {

        dispose();
    }

    private void onPrevious() {

        loadLevelList(levelList.getPrevious());
    }

    private void onNext() {

        loadLevelList(levelList.getNext());
    }

    private void loadLevelList(String path) {


        try {

            levelList = levelRepository.getLevelList(path);
        } catch (IOException | URISyntaxException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.failedListLoad.title"),
                    JOptionPane.ERROR_MESSAGE);
        }

        statusLabel.setText(String.format(
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.results.label"),
                levelList.getCount()));

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

    private class LevelListTableModel extends AbstractTableModel {

        private String[] columnNames = {
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col1Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col2Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col3Name"),
                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelListDialog.table.col4Name")
        };

        private Object[][] data = {};

        void updateTable(final ApiBaseList levelList) {

            data = new Object[levelList.getCount()][4];
            for (int ii = 0; ii < levelList.getCount(); ii++) {

                JsonValue value = (JsonValue)levelList.getResults().get(ii);

                data[ii][0] = value.get("name").asString();
                data[ii][1] = value.get("author").asString();
                data[ii][2] = value.get("difficulty").asString();
                data[ii][3] = value.get("description").asString();
            }
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
    }
}
