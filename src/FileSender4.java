import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileSender4 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("XOR_FileTransfer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		
		JPanel panel = new JPanel();
		JTextField tf = new JTextField(20);
		JButton btn = new JButton("CONNECT");
		
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String ipAddress = tf.getText();
				try {
					connectServer(ipAddress);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		panel.add(new JLabel("Input IP Address"));
		panel.add(tf);
		panel.add(btn);
		
		contentPane.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	
	}

	private static void connectServer(String ipAddress) throws IOException {
		Selector selector = Selector.open();
		SocketChannel connectionClient = SocketChannel.open();
		connectionClient.configureBlocking(false);
		connectionClient.connect(new InetSocketAddress(ipAddress, 1234));
		connectionClient.register(selector, SelectionKey.OP_CONNECT);
		
		JFrame frame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();
		
		while(true) {
			selector.select();
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while(iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				SocketChannel client = (SocketChannel) key.channel();
				
				if(key.isConnectable()) {
					if(client.isConnectionPending()) {
						System.out.println("Trying to finish connection.");
						client.finishConnect();
					}
					client.register(selector, SelectionKey.OP_WRITE);
					continue;
				}
				
				if(key.isWritable()) {
					int result = fileChooser.showOpenDialog(frame);
			         
			        if (result == JFileChooser.APPROVE_OPTION) {
			            //선택한 파일의 경로 반환
			            String sendFileLocation = fileChooser.getSelectedFile().getPath();
						sendFile(client, sendFileLocation);
			        }
			        
					client.close();
					return;
				}
				
			}
		}
	
	}
	
	private static void sendFile(SocketChannel client, String sendFileLocation) throws IOException {
		//String fName = "C:\\Test\\jdk-8u111-windows-x64.exe";
		//String fName = "C:\\Test\\The.Holiday.2006.XviD.AC3.CD1-WAF.avi";
		String fName = sendFileLocation;
		int bufferSize = 64 * 1024 * 1024;
		Path path = Paths.get(fName);
		FileChannel fileChannel = FileChannel.open(path);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		
		// timer start
		StartTime timer = new StartTime(0);
		Random rand = new Random();
		int noOfBytesRead = 0;
		int noOfBytesWrite = 0;
		int counter = 0;
		
		System.out.println("Start timer, file downloading.");

		do {
			noOfBytesRead = fileChannel.read(buffer);
			if (noOfBytesRead <= 0 ) break;
			counter += noOfBytesRead;
			buffer.flip();
			do {
				if (rand.nextInt(100) < 95) {
					noOfBytesWrite = client.write(buffer);
				}
				else {
					noOfBytesWrite = 0;
				}

				noOfBytesRead -= noOfBytesWrite;
			} while (noOfBytesRead > 0);
			
			buffer.clear();
			
		} while(true);
		
		int transferTime = timer.getTimeElapsed() / 1000;
		System.out.println("TransferTime : " + transferTime + " seconds. ");
		fileChannel.close();
		
	}
	
}