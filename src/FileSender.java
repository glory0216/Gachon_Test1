import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class FileSender {
	public static void main(String[] args) {
		final String hostName = args[0];
		FileSender nioClient = new FileSender();
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

		} catch (IOException e) {
			e.printStackTrace();
		}
		return socketChannel;
	}

	public void sendFile(SocketChannel socketChannel) {
		RandomAccessFile aFile = null;
		try {
			File file = new File("C:\\Test\\jdk-8u111-windows-x64.exe");
			aFile = new RandomAccessFile(file, "r");
			FileChannel inChannel = aFile.getChannel();
			// ByteBuffer buffer = ByteBuffer.allocate(1024);
			ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
			int sequenceNumber = 0;
			
			// timer start
			StartTime timer = new StartTime(0);

			while (inChannel.read(buffer) > 0) {
				buffer.flip();
				sequenceNumber += 1;
				
				Random rand = new Random();
	            if (rand.nextInt(100) < 95) {
	            	socketChannel.write(buffer);
	            }
				
				buffer.clear();
			}
			
			int transferTime = timer.getTimeElapsed() / 1000;
			System.out.println("KB : " + transferTime + " seconds. ");
			Thread.sleep(1000);
			System.out.println("End of file reached..");
			socketChannel.close();
			aFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}