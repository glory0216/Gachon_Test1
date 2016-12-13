import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class FileSender2 {
	public static void main(String[] args) {
		final String hostName = args[0];
		FileSender2 nioClient = new FileSender2();
		SocketChannel socketChannel = nioClient.createChannel(hostName);
		nioClient.sendFile(socketChannel);
	}

	/**
	 * Establishes a socket channel connection
	 *
	 * @return
	 */
	public SocketChannel createChannel(String hostName) {

		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
			SocketAddress socketAddress = new InetSocketAddress(hostName, 1234);
			socketChannel.connect(socketAddress);
			System.out.println("Connected..Now sending the file");
			//socketChannel.configureBlocking(false);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return socketChannel;
	}

	public void sendFile(SocketChannel socketChannel) {
		try { 
			//Path path = Paths.get("C:\\Test\\The.Holiday.2006.XviD.AC3.CD1-WAF.avi");
			Path path = Paths.get("C:\\Test\\jdk-8u111-windows-x64.exe");
			FileChannel inChannel = FileChannel.open(path);
			ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
			
	        int sequenceNumber = 0;

	        // packet 재전송 시 count할 counter
	        int retransmissionCounter = 0;
			
			// timer start
			StartTime timer = new StartTime(0);
			
			while (inChannel.read(buffer) > 0) {
				sequenceNumber += 1;
				buffer.flip();
				
				/*
				Random rand = new Random();
				if (rand.nextInt(100) < 95) {
	            	socketChannel.write(buffer);
				} */
				
				socketChannel.write(buffer);
				buffer.clear();
			}
			
			int transferTime = timer.getTimeElapsed() / 1000;
			System.out.println("KB : " + transferTime + " seconds. ");
			Thread.sleep(1000);
			System.out.println("End of file reached..");
			socketChannel.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	

}