package gameboj.gui;

import gameboj.GameBoy;
import gameboj.component.Joypad.Key;
import gameboj.component.cartridge.Cartridge;
import gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;

public class Main extends Application {

	private final static HashMap<KeyCode, Key> buttonMap;
	static {
		buttonMap = new HashMap<>();
		buttonMap.put(KeyCode.ENTER, Key.START);
		buttonMap.put(KeyCode.SHIFT, Key.SELECT);
		buttonMap.put(KeyCode.A, Key.A);
		buttonMap.put(KeyCode.B, Key.B);
		buttonMap.put(KeyCode.UP, Key.UP);
		buttonMap.put(KeyCode.DOWN, Key.DOWN);
		buttonMap.put(KeyCode.LEFT, Key.LEFT);
		buttonMap.put(KeyCode.RIGHT, Key.RIGHT);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		if (getParameters().getRaw().size() > 1)
			System.exit(1);



		File romFile = new File(getParameters().getRaw().get(0));
		Cartridge rom = Cartridge.ofFile(romFile);
		GameBoy gb = new GameBoy(rom);

		ImageView imageView = new ImageView();
		imageView.setFitWidth(LcdController.LCD_WIDTH * 2);
		imageView.setFitHeight(LcdController.LCD_HEIGHT * 2);
		imageView.setImage(ImageConverter.convert(gb.lcdController().currentImage()));

		BorderPane root = new BorderPane(imageView);
		Scene scene = new Scene(root);
		stage.setTitle("GameBoj");
		stage.setScene(scene);
		stage.show();

		scene.setOnKeyPressed(event -> keyPressedHandler(gb, event, scene));
		scene.setOnKeyReleased(event -> keyReleasedHandler(gb, event, scene));

		long start = System.nanoTime();
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				gb.runUntil((long) ((now - start) * GameBoy.CLOCK_NANO_FREQ));
				imageView.setImage(ImageConverter.convert(gb.lcdController().currentImage()));
			}
		}.start();
	}

	private void keyPressedHandler(GameBoy gb, KeyEvent event, Scene scene) {
		if (event.getCode() == KeyCode.S) {
			if (event.isControlDown())
				gb.getRom().saveGame();
		} else {
			gb.joypad().keyPressed(buttonMap.getOrDefault(event.getCode(), null));
		}
	}

	private void keyReleasedHandler(GameBoy gb, KeyEvent event, Scene scene) {
		gb.joypad().keyReleased(buttonMap.getOrDefault(event.getCode(), null));
	}

}
