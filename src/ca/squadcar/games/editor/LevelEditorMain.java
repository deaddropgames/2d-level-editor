package ca.squadcar.games.editor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.JSeparator;
import java.util.ResourceBundle;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JButton;

public class LevelEditorMain {

	private JFrame frmSquadcarGamesLevel;
	private JLabel lblLeftStatuslabel;
	private JLabel lblRightStatuslabel;
	private JToggleButton tglbtnLineMode;
	private JScrollPane scrollPane;
	private JButton btnZoomIn;
	private JButton btnZoomOut;
	
	private LevelCanvas canvas;
	private boolean inDrawingMode;
	private LevelEditorSettings settings;
	private float zoomFactor;
	private PolyLine currPolyLine;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LevelEditorMain window = new LevelEditorMain();
					window.frmSquadcarGamesLevel.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LevelEditorMain() {
		
		zoomFactor = 10.0f;
		currPolyLine = null;
		
		inDrawingMode = false;
		
		settings = new LevelEditorSettings();
		if(!settings.loadFromFile("conf/editor.ini")) {
			
			System.err.println("Failed to load the input settings.");
		}
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSquadcarGamesLevel = new JFrame();
		frmSquadcarGamesLevel.setTitle(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.frmSquadcarGamesLevel.title")); //$NON-NLS-1$ //$NON-NLS-2$
		frmSquadcarGamesLevel.setBounds(100, 100, 800, 600);
		frmSquadcarGamesLevel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmSquadcarGamesLevel.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mnFile.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnFile);
		
		JMenuItem mntmQuit = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmQuit.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		JMenuItem mntmNew = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmNew.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmOpen.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mnFile.add(mntmOpen);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmSave = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmSave.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmSaveAs.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mnFile.add(mntmSaveAs);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		mnFile.add(mntmQuit);
		
		JMenu mnHelp = new JMenu(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mnHelp.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmAbout.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frmSquadcarGamesLevel,
						ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.aboutDlg.text"), ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.aboutDlg.title"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mnHelp.add(mntmAbout);
		frmSquadcarGamesLevel.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmSquadcarGamesLevel.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		tglbtnLineMode = new JToggleButton(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.tglbtnLineMode.text")); //$NON-NLS-1$ //$NON-NLS-2$
		tglbtnLineMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if(evt.getStateChange() == ItemEvent.SELECTED) {
					canvas.setCursor(Cursor.CROSSHAIR_CURSOR);
					inDrawingMode = true;
					lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.drawingModeOn"));
				} else {
					canvas.setCursor(Cursor.DEFAULT_CURSOR);
					inDrawingMode = false;
					endCurrPolyLine();
				}
			}
		});
		toolBar.add(tglbtnLineMode);
		
		btnZoomIn = new JButton(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnZoomIn.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				zoomFactor *= 2;
				updateForZoom();
			}
		});
		toolBar.add(btnZoomIn);
		
		btnZoomOut = new JButton(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnZoomOut.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomFactor /= 2;
				updateForZoom();
			}
		});
		toolBar.add(btnZoomOut);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		frmSquadcarGamesLevel.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		lblLeftStatuslabel = new JLabel(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.text")); //$NON-NLS-1$ //$NON-NLS-2$
		statusPanel.add(lblLeftStatuslabel);
		
		lblRightStatuslabel = new JLabel(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblRightStatuslabel.text"));
		lblRightStatuslabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(lblRightStatuslabel);
		
		canvas = new LevelCanvas();
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if(!inDrawingMode) {
					return;
				}
				
				// left click means add a point
				if(evt.getButton() == MouseEvent.BUTTON1) {
					WorldPoint point = new WorldPoint((evt.getPoint().x / zoomFactor), (evt.getPoint().y / zoomFactor));
					if(currPolyLine == null) {
						currPolyLine = new PolyLine(point);
						canvas.setTempDrawableElement(currPolyLine);
						lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.endPolyLineTip"));
					} else {
						currPolyLine.addPoint(point);
					}
				} else if(evt.getButton() == MouseEvent.BUTTON3) { // right click means end polyline
					endCurrPolyLine();
				}
				
				canvas.repaint();
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent evt) {
				lblRightStatuslabel.setText(String.format("(%.2f, %.2f)", (evt.getPoint().x / zoomFactor), (evt.getPoint().y / zoomFactor)));
			}
		});
		canvas.setCanvasDimension(new Dimension(500, 300));
		canvas.setZoomFactor(zoomFactor);
		
		scrollPane = new JScrollPane(canvas);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		frmSquadcarGamesLevel.getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	private void endCurrPolyLine() {
		
		if(currPolyLine != null && currPolyLine.getPoints().size() > 1) {
			// add to our canvas permanently (deep copy)
			canvas.addDrawableElement(new PolyLine(currPolyLine));
		}
		
		// reset our current polyline
		currPolyLine = null;
		canvas.setTempDrawableElement(null);
		
		if(!inDrawingMode) {
			lblLeftStatuslabel.setText("");
		} else {
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.drawingModeOn"));
		}
	}
	
	private void updateForZoom() {

		canvas.setZoomFactor(zoomFactor);
		canvas.repaint();
		
		// tell the scroll pane to update its scroll bars...
		scrollPane.setViewportView(canvas);
	}
}
