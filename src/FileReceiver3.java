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

public class FileReceiver3 {

	public static void main(String[] args) throws IOException {
		String receiveFileLocation = args[0];
		Selector selector = Selector.open();
		ServerSocketChannel server = ServerSocketChannel.open();
		server.configureBlocking(false);
		server.socket().bind(new InetSocketAddress(1234));
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true) {
			selector.select();
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while(iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				if(key.isAcceptable()) {
					SocketChannel client = server.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ);
					continue;
				}
				
				if(key.isReadable()) {
					SocketChannel channel = (SocketChannel) key.channel();
					receiveFile(channel, receiveFileLocation);
					channel.close();
					return;
				}
				
			}
		}
		
	}
	
	private static void receiveFile(SocketChannel channel, String receiveFileLocation) throws IOException {
		//String outputFile = "/Users/youngkwanglee/Downloads/testDir/test.exe";
		//String outputFile = "C:\\Test\\test.exe";
		String outputFile = receiveFileLocation;
		int bufferSize = 64 * 1024;
		Path path = Paths.get(outputFile);
		FileChannel fileChannel = FileChannel.open(path, 
				EnumSet.of(StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE)
				);
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		int res = 0;
		int counter = 0;
		
		do {
			buffer.clear();
			res = channel.read(buffer);
			//System.out.println(res);
			buffer.flip();
			if(res > 0) {
				fileChannel.write(buffer);
				counter += res;
			}
		} while (res >= 0);
		
		channel.close();
		fileChannel.close();
		
	}
	
}
