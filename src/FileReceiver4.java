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
		// JFrame으로 GUI 구현
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

	// Selector, ServerSocketChannel 생성 및 역할 등록, 분담
	private static void startServer() {
		String receiveFileLocation = "C:\\Test\\test.avi";

		/********************** 
			Sender 부분과 동일	 
		 **********************/
		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(1234));
			server.register(selector, SelectionKey.OP_ACCEPT);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Receiver에서 중단없이 client들의 요청을 지속적으로 처리하기 위한 쓰레드
		Thread thread = new Thread() {
			public void run() {

				while (true) {
					try {
						int keyCount = selector.select();	// selector에 등록된 key 개수 파악
						if (keyCount == 0) {
							continue;
						}

						Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
						while (iterator.hasNext()) {
							SelectionKey key = iterator.next();
							iterator.remove();

							if (key.isAcceptable()) {	// key의 옵션이 OP_ACCEPT인 경우
								// ServerSocketChannel의 accept 메소드로 소켓 생성
								// 생성된 SocketChannel을 비동기 및 selector에 읽기 모드로 등록
								ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
								SocketChannel client = serverSocketChannel.accept();
								client.configureBlocking(false);
								client.register(selector, SelectionKey.OP_READ);
							} else if (key.isReadable()) {	// key의 옵션이 OP_READ인 경우
								// key로부터 SocketChannel을 얻어옴
								SocketChannel channel = (SocketChannel) key.channel();
								// 요청한 클라이언트의 SocketChannel로부터 file을 받음
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
		String outputFile = receiveFileLocation;
		int bufferSize = 64 * 1024 * 1024;
		Path path = Paths.get(outputFile);
		FileChannel fileChannel = FileChannel.open(path,
				EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));

		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		int res = 0;

		do {
			buffer.clear();
			res = channel.read(buffer);

			buffer.flip();
			if (res > 0) {
				fileChannel.write(buffer);
			}
		} while (res >= 0);

		channel.close();
		fileChannel.close();

	}

}
