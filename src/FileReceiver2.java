import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class FileReceiver2 {

	public static void main(String[] args) {
		FileReceiver2 nioServer = new FileReceiver2();
		SocketChannel socketChannel = nioServer.createServerSocketChannel();
		nioServer.readFileFromSocket(socketChannel);
	}

	public SocketChannel createServerSocketChannel() {

		ServerSocketChannel serverSocketChannel = null;
		SocketChannel socketChannel = null;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(1234));
			socketChannel = serverSocketChannel.accept();
			System.out.println("Connection established...." + socketChannel.getRemoteAddress());
			//serverSocketChannel.configureBlocking(false);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return socketChannel;
	}

	/**
	 * Reads the bytes from socket and writes to file
	 *
	 * @param socketChannel
	 */
	public void readFileFromSocket(SocketChannel socketChannel) {
		try {
			//Path path = Paths.get("C:\\Test\\test.exe");
			Path path = Paths.get("/Users/youngkwanglee/Downloads/testDir/test.exe");
			FileChannel fileChannel = FileChannel.open(path, 
					EnumSet.of(StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.WRITE)
					);
			ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
			while (socketChannel.read(buffer) > 0 || buffer.position() > 0) {
				buffer.flip();
				fileChannel.write(buffer);
				buffer.clear();
			}
			Thread.sleep(1000);
			fileChannel.close();
			System.out.println("End of file reached..Closing channel");
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