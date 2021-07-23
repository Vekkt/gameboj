package gameboj.gui;

import gameboj.component.lcd.LcdImage;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ImageConverter {
	private static final int[] COLOR_MAP = new int[] {
			0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
	};

	public static javafx.scene.image.WritableImage convert(LcdImage lcdImage) {
		BufferedImage image = new BufferedImage(lcdImage.width(), lcdImage.height(),
				BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < lcdImage.height(); ++y)
			for (int x = 0; x < lcdImage.width(); ++x)
				image.setRGB(x, y, COLOR_MAP[lcdImage.get(x, y)]);

		return SwingFXUtils.toFXImage(image, null);
	}
}
