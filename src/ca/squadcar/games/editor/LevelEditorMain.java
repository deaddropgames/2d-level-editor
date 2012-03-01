package ca.squadcar.games.editor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;

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

import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class LevelEditorMain {
	
	final private String[] reservedFilenames = {"editor.ini", "config.ini", "test.ini"};
	final private String defaultLevelDir = "levels/";
	final private String defaultTestLevelFilename = "levels/test.ini";

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
	private String currFilename;
	private boolean unsavedChanges;
	private Point viewportCentre;

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
		
		currFilename = null;
		unsavedChanges = false;
		viewportCentre = null;
		
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
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(!checkSavedChanges()) {
					
					return;
				}
				
				resetLevel();
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmOpen.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(!checkSavedChanges()) {
					
					return;
				}
				
				JFileChooser fc = new JFileChooser(new File(defaultLevelDir));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.levelFiles.fileType.text"), 
						"ini");
				fc.setFileFilter(filter);
				int returnVal = fc.showOpenDialog(frmSquadcarGamesLevel);
				if(returnVal == JFileChooser.APPROVE_OPTION) {

					File levelFile = fc.getSelectedFile();
					
					if(!canvas.loadLevelFromFile(levelFile)) {
						
						JOptionPane.showMessageDialog(frmSquadcarGamesLevel,
								ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.invalidLevelFile.text"), 
								ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.invalidLevelFile.title"), 
								JOptionPane.ERROR_MESSAGE);
					}
					
					canvas.repaint();
				}
			}
		});
		mnFile.add(mntmOpen);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmSave = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmSave.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(currFilename == null) {

					if(chooseLevelFilename()) {
						
						saveLevel(currFilename);
						return;
					}
				}
				
				saveLevel(currFilename);
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.mntmSaveAs.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(chooseLevelFilename()) {
					
					saveLevel(currFilename);
				}
			}
		});
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
						ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.aboutDlg.text"), 
						ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.aboutDlg.title"), 
						JOptionPane.INFORMATION_MESSAGE);
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
				
				// preserve the center position we are currently looking at
				viewportCentre = new Point(scrollPane.getViewport().getViewPosition());
				Dimension vpSize = scrollPane.getViewport().getExtentSize();
				
				// get actual centre
				viewportCentre.x += vpSize.width / 2;
				viewportCentre.y += vpSize.height / 2;
				
				// translate to new position
				viewportCentre.x *= 2;
				viewportCentre.y *= 2;
				
				// get top left corner
				viewportCentre.x -= vpSize.width / 2;
				viewportCentre.y -= vpSize.height / 2;
				
				zoomFactor *= 2;	
				updateForZoom();
			}
		});
		toolBar.add(btnZoomIn);
		
		btnZoomOut = new JButton(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnZoomOut.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// preserve the center position we are currently looking at
				viewportCentre = new Point(scrollPane.getViewport().getViewPosition());
				Dimension vpSize = scrollPane.getViewport().getExtentSize();
				
				// get actual centre
				viewportCentre.x += vpSize.width / 2;
				viewportCentre.y += vpSize.height / 2;
				
				// translate to new position
				viewportCentre.x /= 2;
				viewportCentre.y /= 2;
				
				// get top left corner
				viewportCentre.x -= vpSize.width / 2;
				viewportCentre.y -= vpSize.height / 2;
				
				if(viewportCentre.x < 0) {
					
					viewportCentre.x = 0;
				}
				
				if(viewportCentre.y < 0) {
					
					viewportCentre.y = 0;
				}
				
				zoomFactor /= 2;
				updateForZoom();
			}
		});
		toolBar.add(btnZoomOut);
		
		JButton btnTestLevel = new JButton(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnTestLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(!canvas.hasElements()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.text"),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.title"),
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(settings.getSimJar() == null || settings.getSimJar().isEmpty()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.emptySimJar.text"),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.emptySimJar.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				File simJar = new File(settings.getSimJar());
				if(!simJar.exists()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							String.format(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.simJarNotExists.text"),
									settings.getSimJar()),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.simJarNotExists.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// save the current level to test.ini
				// TODO: eventually we may want to handle more arguments...for now we ignore the ${level} param and just use test.ini
				File testFile = new File(defaultTestLevelFilename);
				saveLevel(defaultTestLevelFilename);
				
				// spawn it
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", simJar.getName(), testFile.getAbsolutePath());
				if(simJar.getParent() != null) {
				
					pb.directory(new File(simJar.getParent()));
				}
				
				try {
					
					pb.start();
				} catch (IOException ex) {

					ex.printStackTrace();
				}
			}
		});
		toolBar.add(btnTestLevel);
		
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
					
					unsavedChanges = true;
				} else if(evt.getButton() == MouseEvent.BUTTON3) { // right click means end polyline
					
					endCurrPolyLine();
				}
				
				canvas.repaint();
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent evt) {
				lblRightStatuslabel.setText(String.format("(%.2f, %.2f)", (evt.getPoint().x / zoomFactor), (-evt.getPoint().y / zoomFactor)));
			}
		});
		
		canvas.setCanvasDimension(settings.getCanvisSize());
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
			unsavedChanges = true;
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
		
		// if we zoomed, let's preserve the viewport centre
		if(viewportCentre != null) {
			
			scrollPane.getViewport().setViewPosition(viewportCentre);
		}
	}
	
	private boolean chooseLevelFilename() {
		
		// make sure there is something to save...
		if(!canvas.hasElements()) {
			
			JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.text"),
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.title"),
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		JFileChooser fc;
		if(currFilename != null) {
			
			fc = new JFileChooser(new File(currFilename));
		} else {
			
			// default directory?
			fc = new JFileChooser(new File(defaultLevelDir));
		}

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.levelFiles.fileType.text"), 
				"ini");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(frmSquadcarGamesLevel);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			
			File file = fc.getSelectedFile();
			if(!file.exists() || JOptionPane.showConfirmDialog(fc, ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.existsMsg.text")) == JOptionPane.YES_OPTION) {
				
				// save to disk
				for(String test : reservedFilenames) {
					
					if(test.compareToIgnoreCase(file.getName()) == 0) {
						
						JOptionPane.showMessageDialog(fc, 
								ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.reservedFileMsg.text"),
								ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.reservedFileMsg.title"),
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				
				currFilename = file.getAbsolutePath();
				return true;
			}
		}
		
		// if we get here...user cancelled
		return false;
	}
	
	private void saveLevel(final String filename) {
		
		if(filename == null) {
			
			System.err.println("No current filename to save file to.");
			return;
		}
		
		try {
			
			Ini ini = new Ini();
			canvas.saveToFile(ini, settings);
			if(filename.endsWith(".ini")) {
				
				ini.store(new File(filename));
			} else {
				
				ini.store(new File(filename + ".ini"));
			}
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.levelSaved"));
			if(filename != defaultTestLevelFilename) {
				
				unsavedChanges = false;
			}
		} catch (InvalidFileFormatException ex) {

			ex.printStackTrace();
		} catch (IOException ex) {

			ex.printStackTrace();
		}
	}
	
	private void resetLevel() {
		
		canvas.reset();
		canvas.repaint();
		currFilename = null;
		unsavedChanges = false;
	}
	
	private boolean checkSavedChanges() {
		
		if(unsavedChanges) {
			
			int result = JOptionPane.showConfirmDialog(frmSquadcarGamesLevel, 
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.unsavedChanges.text"),
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.unsavedChanges.title"),
					JOptionPane.YES_NO_OPTION);
			
			if(result == JOptionPane.NO_OPTION || result == JOptionPane.CANCEL_OPTION) {
				
				return false;
			}
		}
		
		return true;
	}
}
