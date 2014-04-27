package com.deaddropgames.editor.gui;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.deaddropgames.editor.elements.QuadraticBezierCurve;
import com.deaddropgames.editor.events.ElementChangedEvent;
import com.deaddropgames.editor.events.IElementChangedListener;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class CurvePanel extends PropertiesPanel implements IElementChangedListener, ChangeListener {

    JSpinner spinner; // for the number of segments

    /**
     * Create the panel.
     */
    public CurvePanel(final QuadraticBezierCurve curve) {

        super(curve);

        setBorder(new TitledBorder(null, ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("CurvePanel.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        WorldPointPanel firstPointPanel = new WorldPointPanel(curve.first,
                ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("CurvePanel.firstTitle"));
        firstPointPanel.addElemChangedListener(this);
        GridBagConstraints gbc_firstPoint = new GridBagConstraints();
        gbc_firstPoint.fill = GridBagConstraints.BOTH;
        gbc_firstPoint.insets = new Insets(0, 0, 5, 0);
        gbc_firstPoint.gridx = 0;
        gbc_firstPoint.gridy = 0;
        add(firstPointPanel, gbc_firstPoint);

        WorldPointPanel secondPointPanel = new WorldPointPanel(curve.second,
                ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("CurvePanel.secondTitle"));
        secondPointPanel.addElemChangedListener(this);
        GridBagConstraints gbc_secondPoint = new GridBagConstraints();
        gbc_secondPoint.fill = GridBagConstraints.BOTH;
        gbc_secondPoint.insets = new Insets(0, 0, 5, 0);
        gbc_secondPoint.gridx = 0;
        gbc_secondPoint.gridy = 1;
        add(secondPointPanel, gbc_secondPoint);

        WorldPointPanel thirdPointPanel = new WorldPointPanel(curve.third,
                ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("CurvePanel.thirdTitle"));
        thirdPointPanel.addElemChangedListener(this);
        GridBagConstraints gbc_thirdPoint = new GridBagConstraints();
        gbc_thirdPoint.fill = GridBagConstraints.BOTH;
        gbc_thirdPoint.insets = new Insets(0, 0, 5, 0);
        gbc_thirdPoint.gridx = 0;
        gbc_thirdPoint.gridy = 2;
        add(thirdPointPanel, gbc_thirdPoint);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 3;
        add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JLabel lblNumberOfSegments = new JLabel(ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("CurvePanel.numSegsLabel"));
        GridBagConstraints gbc_lblNumberOfSegments = new GridBagConstraints();
        gbc_lblNumberOfSegments.fill = GridBagConstraints.BOTH;
        gbc_lblNumberOfSegments.insets = new Insets(0, 0, 5, 0);
        gbc_lblNumberOfSegments.gridx = 0;
        gbc_lblNumberOfSegments.gridy = 0;
        panel.add(lblNumberOfSegments, gbc_lblNumberOfSegments);

        spinner = new JSpinner(new SpinnerNumberModel(curve.numSegments, 2, 1000, 1));
        spinner.addChangeListener(this);
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.fill = GridBagConstraints.BOTH;
        gbc_spinner.gridx = 0;
        gbc_spinner.gridy = 1;
        panel.add(spinner, gbc_spinner);
    }

    @Override
    public void elementChanged(ElementChangedEvent event) {

        element.init();
        fireElemChangedEvent();
    }

    @Override
    public void stateChanged(ChangeEvent event) {

        ((QuadraticBezierCurve)element).numSegments = (Integer)spinner.getValue();
        element.init();
        fireElemChangedEvent();
    }
}
