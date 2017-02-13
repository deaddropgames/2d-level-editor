package com.deaddropgames.editor.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JTextField;

import java.awt.Insets;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;

import com.deaddropgames.editor.pickle.BaseLevel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LevelSaveDialog extends JDialog implements ActionListener, MouseListener {

    final private String[] reservedFilenames = {"biped.json", "draw.json", "level.json", "ski.json", "conf.json"};
    private JTextField txtTitletextfield;
    private JTextField txtAuthortextfield;
    private JComboBox<String> comboDifficulty;
    private JTextArea txtDescriptiontextarea;
    private JTextField txtFilenametextfield;
    private JButton okButton;
    private JButton cancelButton;
    private boolean wasCancelled;
    private String defaultDir;
    private String currFilename;

    public LevelSaveDialog() {

        setModalityType(ModalityType.APPLICATION_MODAL);

        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblTitle = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.lblTitle.text")); //$NON-NLS-1$ //$NON-NLS-2$
            GridBagConstraints gbc_lblTitle = new GridBagConstraints();
            gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
            gbc_lblTitle.anchor = GridBagConstraints.EAST;
            gbc_lblTitle.gridx = 0;
            gbc_lblTitle.gridy = 0;
            contentPanel.add(lblTitle, gbc_lblTitle);
        }
        {
            txtTitletextfield = new JTextField();
            GridBagConstraints gbc_txtTitletextfield = new GridBagConstraints();
            gbc_txtTitletextfield.insets = new Insets(0, 0, 5, 0);
            gbc_txtTitletextfield.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtTitletextfield.gridx = 1;
            gbc_txtTitletextfield.gridy = 0;
            contentPanel.add(txtTitletextfield, gbc_txtTitletextfield);
            txtTitletextfield.setColumns(10);
        }
        {
            JLabel lblAuthor = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.lblAuthor.text")); //$NON-NLS-1$ //$NON-NLS-2$
            GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
            gbc_lblAuthor.anchor = GridBagConstraints.EAST;
            gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
            gbc_lblAuthor.gridx = 0;
            gbc_lblAuthor.gridy = 1;
            contentPanel.add(lblAuthor, gbc_lblAuthor);
        }
        {
            txtAuthortextfield = new JTextField();
            GridBagConstraints gbc_txtAuthortextfield = new GridBagConstraints();
            gbc_txtAuthortextfield.insets = new Insets(0, 0, 5, 0);
            gbc_txtAuthortextfield.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtAuthortextfield.gridx = 1;
            gbc_txtAuthortextfield.gridy = 1;
            contentPanel.add(txtAuthortextfield, gbc_txtAuthortextfield);
            txtAuthortextfield.setColumns(10);
        }
        {
            JLabel lblDifficulty = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.lblDifficulty.text")); //$NON-NLS-1$ //$NON-NLS-2$
            GridBagConstraints gbc_lblDifficulty = new GridBagConstraints();
            gbc_lblDifficulty.anchor = GridBagConstraints.EAST;
            gbc_lblDifficulty.insets = new Insets(0, 0, 5, 5);
            gbc_lblDifficulty.gridx = 0;
            gbc_lblDifficulty.gridy = 3;
            contentPanel.add(lblDifficulty, gbc_lblDifficulty);
        }
        {
            comboDifficulty = new JComboBox<>();
            comboDifficulty.setModel(new DefaultComboBoxModel<>(new String[] {
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.circle"),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.square"),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.diamond"),
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.comboDifficulty.doubleDiamond")
                    }));
            GridBagConstraints gbc_comboBox = new GridBagConstraints();
            gbc_comboBox.insets = new Insets(0, 0, 5, 0);
            gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBox.gridx = 1;
            gbc_comboBox.gridy = 3;
            contentPanel.add(comboDifficulty, gbc_comboBox);
        }
        {
            JLabel lblDescription = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.lblDescription.text"));
            GridBagConstraints gbc_lblDescription = new GridBagConstraints();
            gbc_lblDescription.anchor = GridBagConstraints.NORTHEAST;
            gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
            gbc_lblDescription.gridx = 0;
            gbc_lblDescription.gridy = 4;
            contentPanel.add(lblDescription, gbc_lblDescription);
        }
        {
            txtDescriptiontextarea = new JTextArea();
            txtDescriptiontextarea.setLineWrap(true);
            GridBagConstraints gbc_txtrDescriptiontextarea = new GridBagConstraints();
            gbc_txtrDescriptiontextarea.insets = new Insets(0, 0, 5, 0);
            gbc_txtrDescriptiontextarea.fill = GridBagConstraints.BOTH;
            gbc_txtrDescriptiontextarea.gridx = 1;
            gbc_txtrDescriptiontextarea.gridy = 4;
            contentPanel.add(txtDescriptiontextarea, gbc_txtrDescriptiontextarea);
        }
        {
            JLabel lblFilename = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.lblFilename.text"));
            GridBagConstraints gbc_lblFilename = new GridBagConstraints();
            gbc_lblFilename.anchor = GridBagConstraints.EAST;
            gbc_lblFilename.insets = new Insets(0, 0, 0, 5);
            gbc_lblFilename.gridx = 0;
            gbc_lblFilename.gridy = 5;
            contentPanel.add(lblFilename, gbc_lblFilename);
        }
        {
            txtFilenametextfield = new JTextField();
            txtFilenametextfield.addMouseListener(this);
            txtFilenametextfield.setEditable(false);
            GridBagConstraints gbc_txtFilenametextfield = new GridBagConstraints();
            gbc_txtFilenametextfield.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtFilenametextfield.gridx = 1;
            gbc_txtFilenametextfield.gridy = 5;
            contentPanel.add(txtFilenametextfield, gbc_txtFilenametextfield);
            txtFilenametextfield.setColumns(10);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                okButton = new JButton();
                okButton.addActionListener(this);
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                cancelButton = new JButton(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.cancelButton.text")); //$NON-NLS-1$ //$NON-NLS-2$
                cancelButton.addActionListener(this);
                buttonPane.add(cancelButton);
            }
        }
    }

    public void updateForMeta(final boolean isExport, final String defaultDir, final String currFilename,
                              final com.deaddropgames.editor.pickle.Level level) {

        this.defaultDir = defaultDir;

        if(!isExport && currFilename != null) {

            this.currFilename = currFilename;
        } else {

            this.currFilename = null;
            txtFilenametextfield.setText("");
        }

        // default to this...it will be changed if the save/export button is updated
        wasCancelled = true;

        if(isExport) {

            setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.exportDlg.title"));
            okButton.setText(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.okButton.exportText"));
        } else {

            setTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.saveDlg.title"));
            okButton.setText(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.okButton.saveText"));
        }

        if(level != null) {

            txtTitletextfield.setText(level.getName());
            txtAuthortextfield.setText(level.getAuthor());
            if(level.getDifficulty() < comboDifficulty.getItemCount()) {

                comboDifficulty.setSelectedIndex(level.getDifficulty());
            }
            txtDescriptiontextarea.setText(level.getDescription());
            if(!isExport && currFilename != null) {

                txtFilenametextfield.setText(currFilename);
            }
        } else {

            txtTitletextfield.setText("");
            txtAuthortextfield.setText("");
            comboDifficulty.setSelectedIndex(0);
            txtDescriptiontextarea.setText("");
            txtFilenametextfield.setText("");
        }
    }

    public void updateLevel(BaseLevel level) {

        level.setName(txtTitletextfield.getText());
        level.setAuthor(txtAuthortextfield.getText());
        level.setDifficulty(comboDifficulty.getSelectedIndex());
        level.setDescription(txtDescriptiontextarea.getText());
    }

    public String getFilename() {

        return txtFilenametextfield.getText();
    }

    public boolean wasCancelled() {

        return wasCancelled;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        // if not, show message box and return
        if(event.getSource() == okButton) {

            String errMsg = "";
            if(txtTitletextfield.getText().isEmpty()) {

                errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.title") + "\n";
            }

            if(txtDescriptiontextarea.getText().isEmpty()) {

                errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.desc") + "\n";
            }

            if(txtFilenametextfield.getText().isEmpty()) {

                errMsg += ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.validationError.filename") + "\n";
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
        } else if(event.getSource() == cancelButton) {

            wasCancelled = true;
        }

        // hide the dialog
        setVisible(false);
    }

    @Override
    public void mouseClicked(MouseEvent event) {

        if(event.getSource() == txtFilenametextfield) {

            JFileChooser fc;
            if(currFilename != null) {

                fc = new JFileChooser(new File(currFilename));
            } else {

                fc = new JFileChooser(new File(defaultDir));
            }

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileType.text"),
                    "json");
            fc.setFileFilter(filter);

            fc.setDialogTitle(ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileChooser.title"));
            int returnVal = fc.showDialog(this, ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileChooser.approveText"));
            if(returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();

                // make sure its not using a reserved file name
                for(String reservedName : reservedFilenames) {

                    if(reservedName.compareToIgnoreCase(file.getName()) == 0) {

                        JOptionPane.showMessageDialog(fc,
                                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileChooser.reservedFileMsg.text"),
                                ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileChooser.reservedFileMsg.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // if file exists, ask for confirmation
                if(file.exists()) {

                    if(JOptionPane.showConfirmDialog(fc, ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages").getString("LevelSaveDialog.fileChooser.existsMsg.text")) != JOptionPane.YES_OPTION) {

                        return;
                    }
                }

                // make sure it's got the proper file extension
                String filename = file.getAbsolutePath();
                String test = filename.toLowerCase();
                if(!test.endsWith(".json")) {

                    filename += ".json";
                }

                txtFilenametextfield.setText(filename);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }
}
