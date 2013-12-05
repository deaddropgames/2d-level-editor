package ca.squadcar.games.editor.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.squadcar.games.editor.Globals;
import ca.squadcar.games.editor.elements.Tree;
import ca.squadcar.games.editor.events.ElementChangedEvent;
import ca.squadcar.games.editor.events.IElementChangedListener;

import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class TreePanel extends PropertiesPanel implements IElementChangedListener, ChangeListener {
	
	public static float lastWidth = 3.0f;
	public static float lastHeight = 5.0f;
	public static int lastLevels = 5;
	
	// ui members
	private JSpinner widthSpinner;
	private JSpinner heightSpinner;
	private JSpinner levelsSpinner;

	public TreePanel(final Tree tree) {
		
		super(tree);
		
		ResourceBundle bundle = ResourceBundle.getBundle("ca.squadcar.games.editor.messages");
		
		setBorder(new TitledBorder(null, bundle.getString("TreePanel.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblWidth = new JLabel(bundle.getString("TreePanel.width"));
		GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 0;
		add(lblWidth, gbc_lblWidth);
		
		widthSpinner = new JSpinner(new SpinnerNumberModel(tree.width, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
		widthSpinner.addChangeListener(this);
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		add(widthSpinner, gbc_spinner);
		
		JLabel lblHeight = new JLabel(bundle.getString("TreePanel.height"));
		GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 1;
		add(lblHeight, gbc_lblHeight);
		
		heightSpinner = new JSpinner(new SpinnerNumberModel(tree.height, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
		heightSpinner.addChangeListener(this);
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		add(heightSpinner, gbc_spinner_1);
		
		JLabel lblLevels = new JLabel(bundle.getString("TreePanel.levels"));
		GridBagConstraints gbc_lblLevels = new GridBagConstraints();
		gbc_lblLevels.insets = new Insets(0, 0, 5, 5);
		gbc_lblLevels.gridx = 0;
		gbc_lblLevels.gridy = 2;
		add(lblLevels, gbc_lblLevels);
		
		levelsSpinner = new JSpinner(new SpinnerNumberModel(tree.levels, 0, 1000, 1));
		levelsSpinner.addChangeListener(this);
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_2.gridx = 1;
		gbc_spinner_2.gridy = 2;
		add(levelsSpinner, gbc_spinner_2);
		
		WorldPointPanel firstPointPanel = new WorldPointPanel(tree.location, bundle.getString("TreePanel.location"));
		firstPointPanel.addElemChangedListener(this);
		GridBagConstraints gbc_firstPoint = new GridBagConstraints();
		gbc_firstPoint.fill = GridBagConstraints.BOTH;
		gbc_firstPoint.insets = new Insets(0, 0, 5, 0);
		gbc_firstPoint.gridx = 1;
		gbc_firstPoint.gridy = 3;
		add(firstPointPanel, gbc_firstPoint);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {

		double height = (Double)heightSpinner.getValue();
		double width = (Double)widthSpinner.getValue();
		
		((Tree)element).height = (float)height;
		((Tree)element).width = (float)width;
		((Tree)element).levels = (Integer)levelsSpinner.getValue();
		((Tree)element).init();
		
		// update the last values
		TreePanel.lastHeight = (float)height;
		TreePanel.lastWidth = (float)width;
		TreePanel.lastLevels = (Integer)levelsSpinner.getValue();
		
		fireElemChangedEvent();
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		
		((Tree)element).init();
		fireElemChangedEvent();
	}
}
