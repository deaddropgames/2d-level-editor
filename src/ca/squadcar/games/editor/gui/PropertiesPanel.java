package ca.squadcar.games.editor.gui;

import java.util.ArrayList;

import javax.swing.JPanel;

import ca.squadcar.games.editor.elements.IDrawableElement;
import ca.squadcar.games.editor.events.ElementChangedEvent;
import ca.squadcar.games.editor.events.IElementChangedListener;

@SuppressWarnings("serial")
public abstract class PropertiesPanel extends JPanel {

	protected IDrawableElement element;
	protected ArrayList<IElementChangedListener> elemChangedListeners = new ArrayList<IElementChangedListener>();
	
	/**
	 * Create the panel.
	 */
	public PropertiesPanel(final IDrawableElement element) {
		
		this.element = element; // reference to the element
	}
	
	public void addElemChangedListener(IElementChangedListener listener) {
		
		elemChangedListeners.add(listener);
	}
	
	public void removeElemChangedListener(IElementChangedListener listener) {
		
		elemChangedListeners.remove(listener);
	}
	
	public IDrawableElement getElement() {
		
		return element;
	}
	
	protected void fireElemChangedEvent() {
		
		// let the parents know that there was a change
		ElementChangedEvent event = new ElementChangedEvent(this);
		for(IElementChangedListener listener : elemChangedListeners) {
			
			listener.elementChanged(event);
		}
	}
}
