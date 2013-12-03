package ca.squadcar.games.editor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
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

import javax.swing.ImageIcon;
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

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;

import ca.squadcar.games.editor.elements.IDrawableElement;
import ca.squadcar.games.editor.elements.Line;
import ca.squadcar.games.editor.elements.QuadraticBezierCurve;
import ca.squadcar.games.editor.elements.WorldPoint;
import ca.squadcar.games.editor.events.ElementChangedEvent;
import ca.squadcar.games.editor.events.IElementChangedListener;
import ca.squadcar.games.editor.gui.LevelCanvas;
import ca.squadcar.games.editor.gui.LevelSaveDialog;
import ca.squadcar.games.editor.gui.PropertiesPanel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;

public class LevelEditorMain implements IElementChangedListener, MouseListener {
	
	final private String defaultLevelDir = "levels/";
	final private String defaultTestLevelFilename = "export/test.json";
	final private String defaultExportDir = "export/";
	final private String editorConfigFilename = "conf/editor.json";

	private JFrame frmSquadcarGamesLevel;
	private JLabel lblLeftStatuslabel;
	private JLabel lblRightStatuslabel;

	// west toolbar buttons
	private JToggleButton tglbtnSelect;
	private JToggleButton tglbtnAddLine;
	private JToggleButton tglbtnAddCurve;
	private JButton btnDelete;
	
	private JScrollPane scrollPane;
	private JButton btnZoomIn;
	private JButton btnZoomOut;
	private JPanel propertiesPanel;
	private PropertiesPanel currElemPropsPanel;
	
	private LevelCanvas canvas;
	private boolean inDrawingMode;
	private EditorSettings editorSettings;
	private String currFilename;
	private boolean unsavedChanges;
	private Point viewportCentre;
	private WorldPoint mousePressedPoint; // when a use clicks and holds on a point...
	private boolean justDragged;
	private boolean currElemIsNew; // is true when we have started a new polyline (i.e. not connected to a previous element)
	
	// drawable element references
	private WorldPoint lastPoint;
	private QuadraticBezierCurve currCurve;
	
	// dialogs
	private LevelSaveDialog saveDlg;

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
		
		inDrawingMode = false;
		mousePressedPoint = null;
		justDragged = false;
		currElemIsNew = false;
		
		editorSettings = new EditorSettings();
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new FileReader(editorConfigFilename));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
			
			Gson gson = new Gson();
			editorSettings = gson.fromJson(sb.toString(), EditorSettings.class);
		} catch(Exception ex) {
			
			ex.printStackTrace();
			editorSettings = new EditorSettings();
		} finally {
			
			if(br != null) {
				
				try {
					
					br.close();
				} catch (IOException ex) {
					
					ex.printStackTrace();
				}
			}
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
		frmSquadcarGamesLevel.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent evt) {
				
				if(!checkSavedChanges()) {
					
					return;
				}
				System.exit(0);
			}
		});
		frmSquadcarGamesLevel.setTitle(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.frmSquadcarGamesLevel.title")); //$NON-NLS-1$ //$NON-NLS-2$
		frmSquadcarGamesLevel.setBounds(100, 100, 800, 600);
		frmSquadcarGamesLevel.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmSquadcarGamesLevel.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mnFile.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnFile);
		
		JMenuItem mntmQuit = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmQuit.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(!checkSavedChanges()) {
					
					return;
				}
				System.exit(0);
			}
		});
		
		JMenuItem mntmNew = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmNew.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				newLevel();
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmOpen.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				openLevel();
			}
		});
		mnFile.add(mntmOpen);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmSave = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmSave.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				saveLevel();
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmSaveAs.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				saveLevelAs();
			}
		});
		mnFile.add(mntmSaveAs);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmExport = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmExport.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmExport.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {

				if(chooseLevelFilename(true, defaultExportDir)) {
						
					exportLevel(currFilename);
					return;
				}
			}
		});
		mnFile.add(mntmExport);
		
		JSeparator separator_2 = new JSeparator();
		mnFile.add(separator_2);
		mnFile.add(mntmQuit);
		
		JMenu mnDraw = new JMenu(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mnDraw.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnDraw);
		
		JMenuItem mntmEdit = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmSelect.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmEdit.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				setSelectMode();
			}
		});
		mntmEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
		mnDraw.add(mntmEdit);
		
		JSeparator separator_5 = new JSeparator();
		mnDraw.add(separator_5);
		
		JMenuItem mntmLine = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmLine.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmLine.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				setLineMode();
			}
		});
		mntmLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
		mnDraw.add(mntmLine);
		
		JMenuItem mntmBezierCurve = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmBezierCurve.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmBezierCurve.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				setCurveMode();
			}
		});
		mntmBezierCurve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
		mnDraw.add(mntmBezierCurve);
		
		JSeparator separator_6 = new JSeparator();
		mnDraw.add(separator_6);
		
		JMenuItem mntmDeleteSelected = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmDeleteSelected.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmDeleteSelected.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				deleteSelectedElement();
			}
		});
		mntmDeleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		mnDraw.add(mntmDeleteSelected);
		
		JMenu mnHelp = new JMenu(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mnHelp.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.mntmAbout.text")); //$NON-NLS-1$ //$NON-NLS-2$
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
		
		btnZoomIn = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/zoom_in.png")));
		btnZoomIn.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnZoomIn.toolTip"));
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
				
				canvas.zoomIn(2);	
				updateForZoom();
			}
		});
		
		JButton btnNew = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/page.png")));
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				newLevel();
			}
		});
		btnNew.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnNew.toolTip"));
		toolBar.add(btnNew);
		
		JButton btnOpen = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/folder_page.png")));
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				openLevel();
			}
		});
		btnOpen.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnOpen.toolTip"));
		toolBar.add(btnOpen);
		
		JButton btnSave = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/disk.png")));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				saveLevel();
			}
		});
		btnSave.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnSave.toolTip"));
		toolBar.add(btnSave);
		
		JButton btnSaveAs = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/page_save.png")));
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				saveLevelAs();
			}
		});
		btnSaveAs.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnSaveAs.toolTip"));
		toolBar.add(btnSaveAs);
		
		JButton btnExport = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/page_lightning.png")));
		btnExport.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(chooseLevelFilename(true, defaultExportDir)) {
					
					exportLevel(currFilename);
					return;
				}
			}
		});
		btnExport.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnExport.toolTip"));
		toolBar.add(btnExport);
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		separator_4.setMaximumSize(new Dimension(10, 16));
		toolBar.add(separator_4);
		toolBar.add(btnZoomIn);
		
		btnZoomOut = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/zoom_out.png")));
		btnZoomOut.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnZoomOut.toolTip"));
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
				
				canvas.zoomOut(2);
				updateForZoom();
			}
		});
		toolBar.add(btnZoomOut);
		
		JButton btnTestLevel = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/application_go.png")));
		btnTestLevel.setHorizontalAlignment(SwingConstants.LEFT);
		btnTestLevel.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.toolTip"));
		btnTestLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(!canvas.hasElements()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.text"),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.title"),
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(editorSettings.simJar == null || editorSettings.simJar.isEmpty()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.emptySimJar.text"),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnTestLevel.emptySimJar.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				File simJar = new File(editorSettings.simJar);
				if(!simJar.exists()) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
							String.format(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
								getString("LevelEditorMain.btnTestLevel.simJarNotExists.text"),
									editorSettings.simJar),
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
								getString("LevelEditorMain.btnTestLevel.simJarNotExists.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// save the current level to test.json
				File testFile = new File(defaultTestLevelFilename);
				exportLevel(defaultTestLevelFilename);
				
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
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setMaximumSize(new Dimension(10, 16));
		separator_3.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_3);
		toolBar.add(btnTestLevel);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		frmSquadcarGamesLevel.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		lblLeftStatuslabel = new JLabel(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.lblLeftStatuslabel.text")); //$NON-NLS-1$ //$NON-NLS-2$
		statusPanel.add(lblLeftStatuslabel);
		
		lblRightStatuslabel = new JLabel(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
				getString("LevelEditorMain.lblRightStatuslabel.text"));
		lblRightStatuslabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(lblRightStatuslabel);
		
		canvas = new LevelCanvas();
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			
			@Override
			public void mouseMoved(MouseEvent evt) {
				
				float zoomFactor = canvas.getZoomFactor();
				WorldPoint point = new WorldPoint(evt.getPoint().x / zoomFactor, evt.getPoint().y / zoomFactor);
				if(!inDrawingMode) {
					
					if(canvas.hitTest(evt.getX(), evt.getY())) {
						
						canvas.setCursor(Cursor.HAND_CURSOR);
					} else {
						
						canvas.setCursor(Cursor.DEFAULT_CURSOR);
					}
				} else { // we are in drawing mode, so if the last point is defined, draw a temp line
					
					if(lastPoint != null) {
						
						canvas.setGuideLine(new Line(lastPoint, point));
						canvas.repaint();
					}
				}
				
				lblRightStatuslabel.setText(String.format("(%.2f, %.2f)", point.x, -point.y));
			}
			
			@Override
			public void mouseDragged(MouseEvent evt) {
				
				if(mousePressedPoint != null) {
					
					float zoomFactor = canvas.getZoomFactor();
					mousePressedPoint.x = (evt.getPoint().x / zoomFactor);
					mousePressedPoint.y = (evt.getPoint().y / zoomFactor);
					
					IDrawableElement hitElement = canvas.getLastHitElement();
					if(hitElement != null) {
						
						// update for it's motion
						hitElement.init();
						
						// if the element that changed has neighbors, we will need to update them as well
						canvas.updateNeighbors(hitElement);
						
						canvas.repaint();
						
						unsavedChanges = true;
						justDragged = true;
					} else { // this shouldn't happen, but just in case...
						
						mousePressedPoint = null;
					}
				}
			}
		});
		
		canvas.setCanvasDimension(new Dimension(editorSettings.canvasWidth, editorSettings.canvasHeight));
		canvas.setZoomFactor(10.0f);
		
		scrollPane = new JScrollPane(canvas);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				
				// move the biped reference
				Point point = scrollPane.getViewport().getViewPosition();
				float zoomFactor = canvas.getZoomFactor();
				canvas.updateForViewportChange(new Point(Math.round(point.x / zoomFactor), Math.round(point.y / zoomFactor)));
				canvas.repaint();
			}
		});
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				
				// move the biped reference
				Point point = scrollPane.getViewport().getViewPosition();
				float zoomFactor = canvas.getZoomFactor();
				canvas.updateForViewportChange(new Point(Math.round(point.x / zoomFactor), Math.round(point.y / zoomFactor)));
				canvas.repaint();
			}
		});
		
		frmSquadcarGamesLevel.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JToolBar westToolBar = new JToolBar();
		frmSquadcarGamesLevel.getContentPane().add(westToolBar, BorderLayout.WEST);
		westToolBar.setOrientation(SwingConstants.VERTICAL);
		westToolBar.setFloatable(false);
		
		tglbtnSelect = new JToggleButton(new ImageIcon(LevelEditorMain.class.getResource("icons/cursor.png")));
		
		tglbtnSelect.setSelected(true);
		tglbtnSelect.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.tglbtnSelect.toolTip"));
		westToolBar.add(tglbtnSelect);
		
		tglbtnAddLine = new JToggleButton(new ImageIcon(LevelEditorMain.class.getResource("icons/chart_line_add.png")));
		tglbtnAddLine.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.tglbtnAddLine.toolTip"));
		westToolBar.add(tglbtnAddLine);
		
		tglbtnAddCurve = new JToggleButton(new ImageIcon(LevelEditorMain.class.getResource("icons/chart_curve_add.png")));
		tglbtnAddCurve.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.tglbtnAddCurve.toolTip"));
		westToolBar.add(tglbtnAddCurve);
		
		btnDelete = new JButton(new ImageIcon(LevelEditorMain.class.getResource("icons/delete.png")));
		btnDelete.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				
				deleteSelectedElement();
			}
		});
		btnDelete.setBorder(new EmptyBorder(8, 7, 8, 7));
		btnDelete.setToolTipText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.btnDelete.toolTip"));
		btnDelete.setEnabled(false);
		westToolBar.add(btnDelete);
		
		tglbtnSelect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				
				if(tglbtnSelect.isSelected()) {
					
					setSelectMode();
				} else {
					
					// default to line mode if deselected
					setLineMode();
				}
			}
		});
		
		tglbtnAddLine.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				
				if(tglbtnAddLine.isSelected()) {
					
					setLineMode();
				} else {
					
					// default to edit mode if deselected
					setSelectMode();
				}
			}
		});
		
		tglbtnAddCurve.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				
				if(tglbtnAddCurve.isSelected()) {
					
					setCurveMode();
				} else {
					
					// default to edit mode if deselected
					setSelectMode();
				}
			}
		});
		
		propertiesPanel = new JPanel();
		frmSquadcarGamesLevel.getContentPane().add(propertiesPanel, BorderLayout.EAST);
		propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
		
		saveDlg = new LevelSaveDialog();
		
		frmSquadcarGamesLevel.setLocationRelativeTo(null);
	}
	
	private void updateForZoom() {

		canvas.repaint();
		
		// tell the scroll pane to update its scroll bars...
		scrollPane.setViewportView(canvas);
		
		// if we zoomed, let's preserve the viewport centre
		if(viewportCentre != null) {
			
			scrollPane.getViewport().setViewPosition(viewportCentre);
			
			// move the biped reference
			float zoomFactor = canvas.getZoomFactor();
			canvas.updateForViewportChange(new Point(Math.round(viewportCentre.x / zoomFactor), Math.round(viewportCentre.y / zoomFactor)));
		}
	}
	
	private boolean chooseLevelFilename(boolean isExport, final String defaultDir) {
		
		// make sure there is something to save...
		if(!canvas.hasElements()) {
			
			JOptionPane.showMessageDialog(frmSquadcarGamesLevel, 
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.text"),
					ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.nothingToSave.title"),
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		saveDlg.updateForMeta(isExport, defaultDir, currFilename, canvas.getLevelForSave());
		saveDlg.setLocationRelativeTo(frmSquadcarGamesLevel);
		saveDlg.setVisible(true);
		
		if(saveDlg.wasCancelled()) {
			
			return false;
		}

		currFilename = saveDlg.getFilename();
		
		// if we get here...user cancelled
		return true;
	}
	
	private void saveLevel(final String filename) {
		
		if(filename == null) {
			
			System.err.println("No current filename to save file to.");
			return;
		}
		
		// NOTE: the json level class is different from those that can be exported
		JsonLevel level = canvas.getLevelForSave();
		if(level == null) {
			
			System.err.println("Failed to get the JSON level for saving.");
			return;
		}
		
		saveDlg.updateSaveLevel(level);
		
		Gson json = new GsonBuilder().setPrettyPrinting().create();
		try {

			String data = json.toJson(level);
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(data);
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		unsavedChanges = false;
		lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.levelSaved"));
	}
	
	/**
	 * Exports the file into a format that the games can read
	 */
	private void exportLevel(final String filename) {
		
		if(filename == null) {
			
			System.err.println("Level cannot be saved with no elements.");
			return;
		}
		
		// save it in original format first...
		if(currFilename != null) {
			
			saveLevel(currFilename);
		}
		
		// NOTE: the export level class is different from those that can be saved
		ca.squadcar.games.editor.export.Level level = canvas.getLevelForExport();
		if(level == null) {
			
			System.err.println("Level cannot be exported with no elements.");
			return;
		}
		
		saveDlg.updateExportLevel(level);
		
		// we still consider their to be saved changes on an export...
		Gson json = new GsonBuilder().setPrettyPrinting().create();
		try {

			String data = json.toJson(level);
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(data);
			out.close();
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.levelExported"));
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	private void resetLevel() {
		
		lastPoint = null;
		canvas.reset();
		canvas.repaint();
		currFilename = null;
		unsavedChanges = false;
		
		if(currElemPropsPanel != null) {
			
			propertiesPanel.remove(currElemPropsPanel);
			frmSquadcarGamesLevel.validate();
		}
		
		scrollPane.getViewport().setViewPosition(new Point(0, 0));
		
		btnDelete.setEnabled(false);
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
	
	private void setSelectMode() {
		
		canvas.setCursor(Cursor.DEFAULT_CURSOR);
		canvas.setTempDrawableElement(null);
		inDrawingMode = false;
		tglbtnSelect.setSelected(true);
		tglbtnAddLine.setSelected(false);
		tglbtnAddCurve.setSelected(false);
		canvas.setGuideLine(null);
		lastPoint = null;
		canvas.repaint();
		lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.selectMode"));
	}
	
	private void setLineMode() {
		
		canvas.setCursor(Cursor.CROSSHAIR_CURSOR);
		canvas.setTempDrawableElement(null);
		canvas.selectNone();
		currCurve = null;
		inDrawingMode = true;
		tglbtnAddLine.setSelected(true);
		tglbtnSelect.setSelected(false);
		tglbtnAddCurve.setSelected(false);
		btnDelete.setEnabled(false);
		
		if(currElemPropsPanel != null) {
			
			propertiesPanel.remove(currElemPropsPanel);
			frmSquadcarGamesLevel.validate();
		}
		
		if(lastPoint != null) {
		
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.lineMode"));
		} else {
			
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.addFirstPoint"));
		}
	}
	
	private void setCurveMode() {
		
		canvas.setCursor(Cursor.CROSSHAIR_CURSOR);
		canvas.setTempDrawableElement(null);
		canvas.selectNone();
		currCurve = null;
		inDrawingMode = true;
		tglbtnAddCurve.setSelected(true);
		tglbtnSelect.setSelected(false);
		tglbtnAddLine.setSelected(false);
		btnDelete.setEnabled(false);
		
		if(currElemPropsPanel != null) {
			
			propertiesPanel.remove(currElemPropsPanel);
			frmSquadcarGamesLevel.validate();
		}
		
		if(lastPoint != null) {
			
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.curveModeSecond"));
		} else {
			
			lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.lblLeftStatuslabel.addFirstPoint"));
		}
	}
	
	private void newLevel() {
		
		if(!checkSavedChanges()) {
			
			return;
		}
		
		resetLevel();
	}
	
	private void openLevel() {
		
		if(!checkSavedChanges()) {
			
			return;
		}
		
		resetLevel();
		
		JFileChooser fc = new JFileChooser(new File(defaultLevelDir));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.levelFiles.fileType.text"), 
				"json");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(frmSquadcarGamesLevel);
		if(returnVal == JFileChooser.APPROVE_OPTION) {

			File levelFile = fc.getSelectedFile();
			try {
				
				if(!canvas.loadLevelFromFile(levelFile)) {
					
					JOptionPane.showMessageDialog(frmSquadcarGamesLevel,
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.invalidLevelFile.text"), 
							ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LevelEditorMain.fileChooser.invalidLevelFile.title"), 
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (HeadlessException ex) {

				ex.printStackTrace();
			} catch (IOException ex) {

				ex.printStackTrace();
			}
			
			currFilename = levelFile.getAbsolutePath();
			
			unsavedChanges = false;
			
			canvas.repaint();
		}
	}
	
	private void saveLevel() {
		
		if(currFilename == null) {

			if(!chooseLevelFilename(false, defaultLevelDir)) {
				
				return;
			}
		} else {
			
			// preserve any of the level's meta so we don't write over it with blank data...
			saveDlg.updateForMeta(false, defaultLevelDir, currFilename, canvas.getLevelForSave());
		}
		
		saveLevel(currFilename);
	}
	
	private void saveLevelAs() {
		
		if(chooseLevelFilename(false, defaultLevelDir)) {
			
			saveLevel(currFilename);
		}
	}
	
	private void deleteSelectedElement() {

		if(currElemPropsPanel != null) {
		
			canvas.deleteElement(currElemPropsPanel.getElement());
			
			propertiesPanel.remove(currElemPropsPanel);
			frmSquadcarGamesLevel.validate();
			
			canvas.selectNone();
			btnDelete.setEnabled(false);
			canvas.repaint();
			
			unsavedChanges = true;
		}
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {

		// if the element that changed has neighbors, we will need to update them as well
		IDrawableElement elem = ((PropertiesPanel)event.getSource()).getElement();
		elem.init();
		canvas.updateNeighbors(elem);
		
		canvas.repaint();
		
		unsavedChanges = true;
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		
	}

	@Override
	public void mouseEntered(MouseEvent evt) {

	}

	@Override
	public void mouseExited(MouseEvent evt) {

	}

	@Override
	public void mousePressed(MouseEvent evt) {

		// if mouse is pressed and held in edit mode, we want to move the selected point, if any
		if(evt.getButton() == MouseEvent.BUTTON1) {
			
			if(!inDrawingMode) {

				// clear out the properties and update all elements to not selected
				if(currElemPropsPanel != null) {
					
					currElemPropsPanel.removeElemChangedListener(this);
					propertiesPanel.remove(currElemPropsPanel);
				}
				
				canvas.selectNone();
				btnDelete.setEnabled(false);
				
				if(canvas.hitTest(evt.getX(), evt.getY())) {
					
					IDrawableElement hitElement = canvas.getLastHitElement();
					if(hitElement != null) {
						
						mousePressedPoint = hitElement.getSelectedPoint();
						hitElement.setSelected(true);
					}
				}
			}
			
			frmSquadcarGamesLevel.validate();
			canvas.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		
		if(evt.getButton() == MouseEvent.BUTTON1) {
		
			if(inDrawingMode) {
				
				float zoomFactor = canvas.getZoomFactor();
				WorldPoint point = new WorldPoint((evt.getPoint().x / zoomFactor), (evt.getPoint().y / zoomFactor));
				unsavedChanges = true;
				canvas.selectNone();
				
				// if we are starting a new drawable element
				if(lastPoint == null) {
					
					// if we clicked on a point, snap to it...
					if(canvas.hitTest(evt.getX(), evt.getY())) {
						
						IDrawableElement hitElement = canvas.getLastHitElement();
						if(hitElement != null && hitElement.getSelectedPoint() != null) {
							
							point = hitElement.getSelectedPoint();
							canvas.setCurrListForElement(hitElement);
							hitElement.setSelected(true); // visual aid to verify we connected to the element...
						}
						
						currElemIsNew = false;
					} else {
						
						currElemIsNew = true; // means we are starting a new chain of drawable elements
					}
					
					if(tglbtnAddLine.isSelected()) {
						
						lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
								getString("LevelEditorMain.lblLeftStatuslabel.lineMode"));
					} else if(tglbtnAddCurve.isSelected()) {
						
						currCurve = new QuadraticBezierCurve(point, editorSettings.numCurveSegments);
						canvas.setTempDrawableElement(currCurve);
						lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
								getString("LevelEditorMain.lblLeftStatuslabel.curveModeSecond"));
					}
				} else { // we are continuing a drawable element
					
					if(tglbtnAddLine.isSelected()) {
						
						// assume the last point is the line start
						canvas.addDrawableElement(new Line(lastPoint, point), currElemIsNew);
						currElemIsNew = false;
					} else if(tglbtnAddCurve.isSelected()) {
						
						if(currCurve == null) {
							
							currCurve = new QuadraticBezierCurve(lastPoint, editorSettings.numCurveSegments);
							currCurve.addPoint(point);
							canvas.setTempDrawableElement(currCurve);
							lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
									getString("LevelEditorMain.lblLeftStatuslabel.curveModeLast"));
						} else {
							
							currCurve.addPoint(point);
							if(currCurve.pointsCount() == 3) {
								
								canvas.addDrawableElement(new QuadraticBezierCurve(currCurve), currElemIsNew);
								currElemIsNew = false;
								currCurve = new QuadraticBezierCurve(point, editorSettings.numCurveSegments);
								canvas.setTempDrawableElement(currCurve);
								lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
										getString("LevelEditorMain.lblLeftStatuslabel.curveModeSecond"));
							} else {
							
								lblLeftStatuslabel.setText(ResourceBundle.getBundle("ca.squadcar.games.editor.messages").
										getString("LevelEditorMain.lblLeftStatuslabel.curveModeLast"));
							}
						}
					}
				}
				
				// save for later...
				lastPoint = new WorldPoint(point);
				
				unsavedChanges = true;
			} else {
				
				if(justDragged) {
					
					justDragged = false;
					canvas.selectNone();
					canvas.repaint();
					return;
				}
				
				// in edit mode
				// show the options pane for this element - element would have been hit by previous call to mouse pressed
				IDrawableElement hitElement = canvas.getLastHitElement();
				if(hitElement != null) {
					
					hitElement.setSelected(true);
					currElemPropsPanel = hitElement.getPropertiesPanel();
					if(currElemPropsPanel != null) {
						
						propertiesPanel.add(currElemPropsPanel);
						currElemPropsPanel.addElemChangedListener(this);
					}
					
					btnDelete.setEnabled(true);
				}
			}
			
			frmSquadcarGamesLevel.validate();
			canvas.repaint();
	
			// clear out the last pressed point - used during mouse dragged events
			mousePressedPoint = null;
		} else if(evt.getButton() == MouseEvent.BUTTON3) { // right click to end line
			
			if(inDrawingMode) {
				
				canvas.setTempDrawableElement(null);
				canvas.setGuideLine(null);
				canvas.cancelCurrList();
				lastPoint = null;
				canvas.repaint();
			}
		}
	}
}
