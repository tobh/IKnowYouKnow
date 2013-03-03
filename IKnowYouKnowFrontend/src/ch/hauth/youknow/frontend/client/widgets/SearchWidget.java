package ch.hauth.youknow.frontend.client.widgets;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hauth.youknow.frontend.client.DemoService;
import ch.hauth.youknow.frontend.client.DemoServiceAsync;
import ch.hauth.youknow.frontend.shared.Identifier;
import ch.hauth.youknow.frontend.shared.ResultInfo;
import ch.hauth.youknow.frontend.shared.Source;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SearchWidget implements IsWidget {
	private static final Logger LOGGER = Logger.getLogger(SearchWidget.class.getName());

	private final DemoServiceAsync demoService = GWT.create(DemoService.class);
	private final VerticalPanel widget;

	public SearchWidget() {
		this.widget = new VerticalPanel();
		HorizontalPanel actionPanel = new HorizontalPanel();
		final TextBox searchField = new TextBox();
		searchField.addStyleName("searchField");
		actionPanel.add(searchField);
		Button button = new Button();
		button.setText("Search");
		final VerticalPanel resultPanel = new VerticalPanel();
		resultPanel.setStyleName("searchResultPanel");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String query = searchField.getText();
				if (query.isEmpty()) {
					ErrorDialog errorDialog = new ErrorDialog("Please specify a search query.");
					errorDialog.center();
					return;
				}
				Identifier identifier = new Identifier(Source.MESSAGES, query);
				demoService.search(identifier, new AsyncCallback<ResultInfo[]>() {
					@Override
					public void onSuccess(ResultInfo[] results) {
						renderResults(results, resultPanel);
					}

					@Override
					public void onFailure(Throwable caught) {
						LOGGER.log(Level.WARNING, "Server problem", caught);
						ErrorDialog errorDialog = new ErrorDialog("Server problem.");
						errorDialog.center();
					}
				});
			}
		});
		actionPanel.add(button);
		this.widget.add(actionPanel);
		this.widget.add(resultPanel);
	}

	private void renderResults(ResultInfo[] results, VerticalPanel resultPanel) {
		resultPanel.clear();
		for (ResultInfo resultInfo : results) {
			ResultInfoPanel resultInfoPanel = new ResultInfoPanel(resultInfo);
			resultInfoPanel.setStyleName("resultInfoPanel");
			resultPanel.add(resultInfoPanel);
		}
	}

	@Override
	public Widget asWidget() {
		return this.widget;
	}
}
