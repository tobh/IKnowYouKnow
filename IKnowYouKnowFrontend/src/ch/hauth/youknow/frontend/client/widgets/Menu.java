package ch.hauth.youknow.frontend.client.widgets;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Menu extends HorizontalPanel {
	private RootPanel rootPanel;

	public Menu(RootPanel rootPanel) {
		this.rootPanel = rootPanel;
		DemoWidget demoWidget = new DemoWidget();
		addMenuEntry("Demo", demoWidget, true);
		addMenuEntry("Search", new SearchWidget(), false);
	}

	private void addMenuEntry(String name, final IsWidget widget, boolean active) {
		final Label entryPanel = new Label(name);
		entryPanel.addStyleName("menuEntry");
		entryPanel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showMenuEntry(entryPanel, widget);
			}
		});
		add(entryPanel);
		if (active) {
			showMenuEntry(entryPanel, widget);
		}
	}

	private void showMenuEntry(final Label menuEntry, final IsWidget widget) {
		rootPanel.clear();
		rootPanel.add(widget);
		for (Widget entry : getChildren()) {
			entry.setStyleName("menuEntry");
		}
		menuEntry.setStyleName("menuEntrySelected");
	}
}
