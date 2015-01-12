package com.deaddropgames.editor.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class SmoothTerrainDialog extends JDialog implements ActionListener, ChangeListener {

    protected JButton smoothButton;
    protected JButton closeButton;
    protected JSpinner minAngleSpinner;
    protected JSpinner clipPercentSpinner;
    protected JLabel numFound;
    private LevelCanvas canvas;

    public SmoothTerrainDialog(LevelCanvas canvas) {

        this.canvas = canvas;

        setModalityType(ModalityType.APPLICATION_MODAL);

        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        ResourceBundle bundle = ResourceBundle.getBundle("com.deaddropgames.editor.gui.messages");
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 1};
        gbl_contentPanel.rowHeights = new int[]{0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblTitle = new JLabel(bundle.getString("SmoothTerrainDialog.minAngle.text"));
            GridBagConstraints gbc_lblTitle = new GridBagConstraints();
            gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
            gbc_lblTitle.anchor = GridBagConstraints.EAST;
            gbc_lblTitle.gridx = 0;
            gbc_lblTitle.gridy = 0;
            contentPanel.add(lblTitle, gbc_lblTitle);
        }
        {
            minAngleSpinner = new JSpinner(new SpinnerNumberModel(270, 180, 360, 10));
            GridBagConstraints gbc_spinner = new GridBagConstraints();
            gbc_spinner.insets = new Insets(0, 0, 5, 0);
            gbc_spinner.anchor = GridBagConstraints.WEST;
            gbc_spinner.gridx = 1;
            gbc_spinner.gridy = 0;
            contentPanel.add(minAngleSpinner, gbc_spinner);
            minAngleSpinner.addChangeListener(this);
        }
        {
            JLabel lblTitle = new JLabel(bundle.getString("SmoothTerrainDialog.clipPercent.text"));
            GridBagConstraints gbc_lblTitle = new GridBagConstraints();
            gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
            gbc_lblTitle.anchor = GridBagConstraints.EAST;
            gbc_lblTitle.gridx = 0;
            gbc_lblTitle.gridy = 1;
            contentPanel.add(lblTitle, gbc_lblTitle);
        }
        {
            clipPercentSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 90, 5));
            GridBagConstraints gbc_spinner = new GridBagConstraints();
            gbc_spinner.insets = new Insets(0, 0, 5, 0);
            gbc_spinner.anchor = GridBagConstraints.WEST;
            gbc_spinner.gridx = 1;
            gbc_spinner.gridy = 1;
            contentPanel.add(clipPercentSpinner, gbc_spinner);
        }
        {
            JLabel lblTitle = new JLabel(bundle.getString("SmoothTerrainDialog.numFound.text"));
            GridBagConstraints gbc_lblTitle = new GridBagConstraints();
            gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
            gbc_lblTitle.anchor = GridBagConstraints.EAST;
            gbc_lblTitle.gridx = 0;
            gbc_lblTitle.gridy = 2;
            contentPanel.add(lblTitle, gbc_lblTitle);
        }
        {
            numFound = new JLabel("0");
            GridBagConstraints gbc_text = new GridBagConstraints();
            gbc_text.insets = new Insets(0, 0, 5, 0);
            gbc_text.anchor = GridBagConstraints.WEST;
            gbc_text.gridx = 1;
            gbc_text.gridy = 2;
            contentPanel.add(numFound, gbc_text);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                smoothButton = new JButton(bundle.getString("SmoothTerrainDialog.smoothButton.text"));
                smoothButton.addActionListener(this);
                buttonPane.add(smoothButton);
                smoothButton.setEnabled(false);

                closeButton = new JButton(bundle.getString("SmoothTerrainDialog.closeButton.text"));
                closeButton.addActionListener(this);
                buttonPane.add(closeButton);
                getRootPane().setDefaultButton(closeButton);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == closeButton) {

            // hide the dialog
            setVisible(false);
        } else if(e.getSource() == smoothButton) {

            // perform smooth operation
            int minAngle = (Integer)minAngleSpinner.getValue();
            float clipPercent = (Integer)clipPercentSpinner.getValue() / 100f;
            canvas.smoothLargeTerrainAngles(minAngle, clipPercent);
            canvas.repaint();
            update();
        }
    }

    public void update() {

        int found = canvas.findLargeTerrainAngles((Integer)minAngleSpinner.getValue());
        numFound.setText(String.valueOf(found));
        smoothButton.setEnabled(found > 0);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if(e.getSource() == minAngleSpinner) {

            update();
        }
    }
}
