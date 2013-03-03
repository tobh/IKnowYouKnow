package ch.hauth.youknow.frontend.client.widgets;

import ch.hauth.youknow.frontend.shared.ResultInfo;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultInfoPanel extends VerticalPanel {
	public ResultInfoPanel(ResultInfo resultInfo) {
		Label titleLabel = new Label(resultInfo.getTitle());
		titleLabel.addStyleName("resultInfoTitle");
		add(titleLabel);
		Label infoLabel = new Label(resultInfo.getInfo());
		infoLabel.addStyleName("resultInfoInfo");
		add(infoLabel);
	}
}
