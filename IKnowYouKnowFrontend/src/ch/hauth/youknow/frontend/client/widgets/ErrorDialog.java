package ch.hauth.youknow.frontend.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ErrorDialog extends DialogBox {
	public ErrorDialog(String message) {
		setText("Error");
		setAnimationEnabled(true);
		final Button closeButton = new Button("OK");
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		VerticalPanel panel = new VerticalPanel();
		panel.addStyleName("dialogVPanel");
		panel.add(new HTML(message));
		panel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		panel.add(closeButton);
		setWidget(panel);
	}
}
