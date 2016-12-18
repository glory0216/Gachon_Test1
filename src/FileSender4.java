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
		// JFrame으로 GUI 구현
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

	// Selector, SocketChannel 생성 및 역할 등록, 분담
	private static void connectServer(String ipAddress) throws IOException {
		// selector, SocketChannel 객체 생성
		Selector selector = Selector.open();
		SocketChannel connectionClient = SocketChannel.open();
		connectionClient.configureBlocking(false);	// SocketChannel 비동기화
		connectionClient.connect(new InetSocketAddress(ipAddress, 1234));	// 입력받은 IP에 SocketChannel 연결
		connectionClient.register(selector, SelectionKey.OP_CONNECT);	// selector에 연결 모드 역할로 채널 등록
		
		JFrame frame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();	// file 선택기 GUI 객체 생성
		
		while(true) {
			selector.select();	// selector에 등록되어있는 key들 반환
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();	// 선택된 키들을 뽑아내기 위한 iterator
			while(iterator.hasNext()) {	// 선택될 키가 있으면
				SelectionKey key = iterator.next();	// 키의 역할을 읽기 위한 key 객체
				iterator.remove();
				
				// 현재 key가 만들어진 채널을 SocketChannel로 캐스팅함
				// 활성화된 채널의 정보를 client 객체로 조작하기 위함
				SocketChannel client = (SocketChannel) key.channel();
																		
				if(key.isConnectable()) {	// 현재 key의 옵션이 OP_CONNECT인 경우
					if(client.isConnectionPending()) {	// 연결이 될 때 연결 과정이 끝난 경우
						System.out.println("Trying to finish connection.");
						client.finishConnect();		// 연결 처리를 끝냄
					}
					client.register(selector, SelectionKey.OP_WRITE);	// selector에 쓰기 역할로 등록
					continue;
				}
				
				if(key.isWritable()) {	// 현재 key의 옵션이 OP_WRITE인 경우
					int result = fileChooser.showOpenDialog(frame);
			         
			        if (result == JFileChooser.APPROVE_OPTION) {
			            // 선택된 파일 경로 반환
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
		String fName = sendFileLocation;
		int bufferSize = 64 * 1024 * 1024;
		Path path = Paths.get(fName);
		FileChannel fileChannel = FileChannel.open(path);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);	// 커널 버퍼 할당
		
		// timer start
		StartTime timer = new StartTime(0);
		Random rand = new Random();
		int noOfBytesRead = 0;	// fileChannel로 부터 읽고, sockectChannel에 쓰고 남은 크기를 저장하기 위한 int 변수
		int noOfBytesWrite = 0;	// socketChannel에 쓴 크기를 저장하기 위한 int 변수
		
		System.out.println("Start timer, file downloading.");

		do {
			noOfBytesRead = fileChannel.read(buffer);	// file의 총 크기를 noOfBytesRead에 저장
			if (noOfBytesRead <= 0 ) break;

			buffer.flip();	// buffer를 재사용하기 위해 위치 재조정
			do {
				if (rand.nextInt(100) < 95) {
					noOfBytesWrite = client.write(buffer);
				}
				else {
					noOfBytesWrite = 0;
				}

				noOfBytesRead -= noOfBytesWrite;	// socketChannel에 쓴 만큼 크기를 빼줌
			} while (noOfBytesRead > 0);
			
			buffer.clear();
			
		} while(true);
		
		int transferTime = timer.getTimeElapsed() / 1000;
		System.out.println("TransferTime : " + transferTime + " seconds. ");
		fileChannel.close();
		
	}
	
}