import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FileReceiver4 {

	private static Selector selector;
	private static ServerSocketChannel server;

	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("XOR_FileTransfer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();

		JPanel panel = new JPanel();
		JButton btnStart = new JButton("START");
		JButton btnStop = new JButton("STOP");

		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startServer();
			}
		});

		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		panel.add(new JLabel("FileTransfer Receiver"));
		panel.add(btnStart);
		panel.add(btnStop);

		contentPane.add(panel);
		frame.pack();
		frame.setVisible(true);

	}

	private static void startServer() {
		String receiveFileLocation = "C:\\Test\\test.avi";

		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(1234));
			server.register(selector, SelectionKey.OP_ACCEPT);

		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread thread = new Thread() {
			public void run() {

				while (true) {
					try {
						int keyCount = selector.select();
						if (keyCount == 0) {
							continue;
						}

						Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
						while (iterator.hasNext()) {
							SelectionKey key = iterator.next();
							iterator.remove();

							if (key.isAcceptable()) {
								ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
								SocketChannel client = serverSocketChannel.accept();
								client.configureBlocking(false);
								client.register(selector, SelectionKey.OP_READ);
							} else if (key.isReadable()) {
								SocketChannel channel = (SocketChannel) key.channel();
								receiveFile(channel, receiveFileLocation);
								channel.close();
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		};

		thread.start();
	}

	private static void receiveFile(SocketChannel channel, String receiveFileLocation) throws IOException {
		// String outputFile = "/Users/youngkwanglee/Downloads/testDir/test.exe";
		// String outputFile = "C:\\Test\\test.exe";
		String outputFile = receiveFileLocation;
		int bufferSize = 64 * 1024;
		Path path = Paths.get(outputFile);
		FileChannel fileChannel = FileChannel.open(path,
				EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));

		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		int res = 0;
		int counter = 0;

		do {
			buffer.clear();
			res = channel.read(buffer);
			// System.out.println(res);

			buffer.flip();
			if (res > 0) {
				fileChannel.write(buffer);
				counter += res;
			}
		} while (res >= 0);

		channel.close();
		fileChannel.close();

	}

}
