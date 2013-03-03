package ch.hauth.youknow.frontend.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hauth.youknow.frontend.client.widgets.ErrorDialog;
import ch.hauth.youknow.frontend.client.widgets.Menu;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Demo implements EntryPoint {
	private static final Logger LOGGER = Logger.getLogger(EntryPoint.class.getName());

	private final DemoServiceAsync demoService = GWT.create(DemoService.class);

	@Override
	public void onModuleLoad() {
		demoService.load(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				Menu menu = new Menu(RootPanel.get("content"));
				RootPanel.get("menu").add(menu);
			}

			@Override
			public void onFailure(Throwable caught) {
				LOGGER.log(Level.WARNING, "Server problem", caught);
				ErrorDialog errorDialog = new ErrorDialog("Server problem.");
				errorDialog.center();
			}
		});
	}
}
