package ch.hauth.youknow.frontend.client.widgets;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hauth.youknow.frontend.client.DemoService;
import ch.hauth.youknow.frontend.client.DemoServiceAsync;
import ch.hauth.youknow.frontend.shared.Identifier;
import ch.hauth.youknow.frontend.shared.ResultInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ToggleIdentifierInfoPanel extends VerticalPanel {
	private static final Logger LOGGER = Logger.getLogger(DemoWidget.class.getName());

	private final DemoServiceAsync demoService = GWT.create(DemoService.class);
	private final Identifier identifier;
	private final VerticalPanel identifierInfoPanel;
	private boolean isInitialized;

	public ToggleIdentifierInfoPanel(Identifier identifier) {
		this.identifier = identifier;
		this.isInitialized = false;
		Label identifierLabel = new Label(identifier.getId());
		this.identifierInfoPanel = new VerticalPanel();
		this.identifierInfoPanel.setVisible(false);
		this.identifierInfoPanel.addStyleName("resultInfoPanel");
		identifierLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ToggleIdentifierInfoPanel.this.isInitialized) {
					ToggleIdentifierInfoPanel.this.isInitialized = true;
					demoService.search(ToggleIdentifierInfoPanel.this.identifier, new AsyncCallback<ResultInfo[]>() {
						@Override
						public void onFailure(Throwable caught) {
							LOGGER.log(Level.WARNING, "Server problem", caught);
							ErrorDialog errorDialog = new ErrorDialog("Server problem.");
							errorDialog.center();
						}

						@Override
						public void onSuccess(ResultInfo[] results) {
							renderResults(results);
						}
					});
				} else {
					ToggleIdentifierInfoPanel.this.identifierInfoPanel.setVisible(
						!ToggleIdentifierInfoPanel.this.identifierInfoPanel.isVisible());
				}
			}
		});
		add(identifierLabel);
		add(identifierInfoPanel);
	}

	private void renderResults(ResultInfo[] results) {
		identifierInfoPanel.clear();
		for (ResultInfo result : results) {
			identifierInfoPanel.add(new ResultInfoPanel(result));
		}
		identifierInfoPanel.setVisible(true);
	}
}
