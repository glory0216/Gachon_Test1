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

public class FileSender3 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String sendFileLocation = args[1];
		Selector selector = Selector.open();
		SocketChannel connectionClient = SocketChannel.open();
		connectionClient.configureBlocking(false);
		connectionClient.connect(new InetSocketAddress(args[0], 1234));
		connectionClient.register(selector, SelectionKey.OP_CONNECT);
		
		while(true) {
			selector.select();
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while(iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				SocketChannel client = (SocketChannel) key.channel();
				
				if(key.isConnectable()) {
					if(client.isConnectionPending()) {
						System.out.println("Trying to finish connection");
						client.finishConnect();
					}
					client.register(selector, SelectionKey.OP_WRITE);
					continue;
				}
				
				if(key.isWritable()) {
					sendFile(client, sendFileLocation);
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
		int bufferSize = 64 * 1024;
		Path path = Paths.get(fName);
		FileChannel fileChannel = FileChannel.open(path);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		
		// timer start
		StartTime timer = new StartTime(0);
		Random rand = new Random();
		int noOfBytesRead = 0;
		int noOfBytesWrite = 0;
		int counter = 0;

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
