package com.deaddropgames.editor.gui;

import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JSpinner;

import com.deaddropgames.editor.Globals;
import com.deaddropgames.editor.elements.WorldPoint;

import java.awt.Insets;

public class WorldPointPanel extends PropertiesPanel implements ChangeListener {

    private JSpinner xSpinner;
    private JSpinner ySpinner;

    /**
     * Create the panel.
     */
    public WorldPointPanel(final WorldPoint point, final String label) {

        super(point);

        setBorder(new TitledBorder(null, label, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JLabel lblX = new JLabel("X:");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        add(lblX, gbc_lblX);

        xSpinner = new JSpinner(new SpinnerNumberModel(point.x, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
        xSpinner.addChangeListener(this);
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.insets = new Insets(0, 0, 5, 0);
        gbc_spinner.gridx = 1;
        gbc_spinner.gridy = 0;
        add(xSpinner, gbc_spinner);

        JLabel lblY = new JLabel("Y:");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.insets = new Insets(0, 0, 0, 5);
        gbc_lblY.gridx = 0;
        gbc_lblY.gridy = 1;
        add(lblY, gbc_lblY);

        // NOTE: y is flipped!!!
        ySpinner = new JSpinner(new SpinnerNumberModel(-point.y, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
        ySpinner.addChangeListener(this);
        GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
        gbc_spinner_1.gridx = 1;
        gbc_spinner_1.gridy = 1;
        add(ySpinner, gbc_spinner_1);
    }

    @Override
    protected void fireElemChangedEvent() {

        // update the point
        double xx = (Double)xSpinner.getValue();
        double yy = -(Double)ySpinner.getValue(); // flip the value back...
        ((WorldPoint)element).x = (float)xx;
        ((WorldPoint)element).y = (float)yy;

        super.fireElemChangedEvent();
    }

    @Override
    public void stateChanged(ChangeEvent event) {

        fireElemChangedEvent();
    }
}
