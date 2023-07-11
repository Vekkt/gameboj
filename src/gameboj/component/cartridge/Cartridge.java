package gameboj.component.cartridge;

import gameboj.component.Component;
import gameboj.component.memory.Rom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static gameboj.Preconditions.checkBits16;
import static gameboj.Preconditions.checkBits8;

/**
 * Represents a cartridge of type 0
 * 
 * @author Francois BURGUET 288683
 * @author Gaietan Renault 283350
 */
public final class Cartridge implements Component {

	private static String cartridgeName;
	private final MBC1 mbc;
	private final static int[] RAM_SIZE = { 0, 2048, 8192, 32768 };

	private Cartridge(MBC1 mbc1) {
		this.mbc = mbc1;
	}

	/**
	 * Creates a cartridge from the specified ROM file. Must be of type 0.
	 * 
	 * @param romFile : a ROM file
	 * @return a cartridge : a cartrige usable by the GameBoy
	 * @throws IOException if a problem is encountered during the reading
	 * @throws IllegalArgumentException if the ROM is not of type 0
	 */
	public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream stream = new FileInputStream(romFile)) {
            cartridgeName = romFile.getName();
            byte[] data = Files.readAllBytes(romFile.toPath());
            return new Cartridge(new MBC1(new Rom(data), RAM_SIZE[data[0x149]], cartridgeName));
        }
	}

	public String getName() {
		return cartridgeName;
	}

	@Override
	public int read(int address) {
		checkBits16(address);
		return mbc.read(address);
	}

	@Override
	public void write(int address, int data) {
		mbc.write(checkBits16(address), checkBits8(data));
	}
	public void saveGame() {
		mbc.save();
	}

}
