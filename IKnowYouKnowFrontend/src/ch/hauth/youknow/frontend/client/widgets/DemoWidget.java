package ch.hauth.youknow.frontend.client.widgets;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hauth.youknow.frontend.client.DemoService;
import ch.hauth.youknow.frontend.client.DemoServiceAsync;
import ch.hauth.youknow.frontend.shared.DemoResult;
import ch.hauth.youknow.frontend.shared.Post;
import ch.hauth.youknow.frontend.shared.Source;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DemoWidget implements IsWidget {
	private static final Logger LOGGER = Logger.getLogger(DemoWidget.class.getName());

	private final DemoServiceAsync demoService = GWT.create(DemoService.class);
	private final VerticalPanel widget;

	public DemoWidget() {
		this.widget = new VerticalPanel();
		final TextArea postMessage = new TextArea();
		postMessage.setFocus(true);
		postMessage.selectAll();
		postMessage.setStyleName("postInput");
		this.widget.add(postMessage);

		final ListBox typeSelect = new ListBox();
		int i = 0;
		for (Source source : Source.values()) {
			typeSelect.addItem(source.name());
			if (source.equals(Source.AUTHORS_WITHOUT_THREAD_STARTERS)) {
				typeSelect.setSelectedIndex(i);
			}
			++i;
		}
		this.widget.add(typeSelect);

		final Label postLabel = new Label();
		final FlexTable resultsTable = new FlexTable();
		final Button sendButton = new Button("Send");
		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String post = postMessage.getText();
				if (post.isEmpty()) {
					ErrorDialog errorDialog = new ErrorDialog("Please specify a message.");
					errorDialog.center();
					return;
				}

				String sourceString = typeSelect.getItemText(typeSelect.getSelectedIndex());
				Source source;
				try {
					source = Source.valueOf(sourceString);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Unsupported identifier type", e);
					ErrorDialog errorDialog = new ErrorDialog("Illegal search type.");
					errorDialog.center();
					return;
				}
				demoService.processPost(source, new Post(post), new AsyncCallback<DemoResult[]>() {
					@Override
					public void onFailure(Throwable caught) {
						LOGGER.log(Level.WARNING, "Server problem", caught);
						ErrorDialog errorDialog = new ErrorDialog("Server problem.");
						errorDialog.center();
					}

					@Override
					public void onSuccess(DemoResult[] results) {
						postLabel.setText(post);
						renderResults(resultsTable, results);
					}
				});
			}
		});
		this.widget.add(sendButton);
		VerticalPanel postResultPanel = new VerticalPanel();
		postResultPanel.setStyleName("postLabel");
		postResultPanel.add(new Label("Post:"));
		postResultPanel.add(postLabel);
		this.widget.add(postResultPanel);
		VerticalPanel postResultDataPanel = new VerticalPanel();
		postResultDataPanel.setStyleName("postResult");
		postResultDataPanel.add(new Label("Results:"));
		postResultDataPanel.add(resultsTable);
		resultsTable.setStyleName("postResultTable");
		renderResults(resultsTable, new DemoResult[0]);
		this.widget.add(postResultDataPanel);
	}

	@Override
	public Widget asWidget() {
		return this.widget;
	}

	private void renderResults(FlexTable resultsPanel, DemoResult[] results) {
		resultsPanel.clear();
		resultsPanel.setCellPadding(3);
		resultsPanel.setCellSpacing(1);
		resultsPanel.setWidget(0, 0, new Label("Score"));
		resultsPanel.setWidget(0, 1, new Label("Result"));
		resultsPanel.getCellFormatter().setStyleName(0, 0, "postResultTableHeader");
		resultsPanel.getCellFormatter().setStyleName(0, 1, "postResultTableHeader");
		for (int i = 0; i < results.length; i++) {
			Label scoreLabel = new Label(NumberFormat.getFormat("0.000000").format(results[i].getScore()));
			resultsPanel.setWidget(i + 1, 0, scoreLabel);
			ToggleIdentifierInfoPanel identifierPanel = new ToggleIdentifierInfoPanel(results[i].getIdentifier());
			resultsPanel.setWidget(i + 1, 1, identifierPanel);
			resultsPanel.getCellFormatter().setStyleName(i + 1, 0, (i % 2 == 0) ? "postResultTableEven" : "postResultTableOdd");
			resultsPanel.getCellFormatter().setStyleName(i + 1, 1, (i % 2 == 0) ? "postResultTableEven" : "postResultTableOdd");
		}
		resultsPanel.getColumnFormatter().setStyleName(0, "postResultTableScoreCol");
		resultsPanel.getColumnFormatter().setStyleName(1, "postResultTableIdentifierCol");
	}
}
