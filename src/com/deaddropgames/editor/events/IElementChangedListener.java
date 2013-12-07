package com.deaddropgames.editor.events;

import java.util.EventListener;

public interface IElementChangedListener extends EventListener {
	
	public void elementChanged(ElementChangedEvent event);
}
