import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {

    private static final long id = IdGenerator.getId();
    private static int clientNumber;
    private static int READ_TIME_OUT = 1000;

    public SocketServer(String addr, int port) throws IOException {
        InetSocketAddress sockAddr = new InetSocketAddress(addr, port);
        AsynchronousServerSocketChannel asyncServerSock =  AsynchronousServerSocketChannel.open().bind(sockAddr);

        asyncServerSock.accept(asyncServerSock, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel >() {

            @Override
            public void completed(AsynchronousSocketChannel channel, AsynchronousServerSocketChannel attachment) {
                asyncServerSock.accept(asyncServerSock, this);
                readWrite(channel, ++clientNumber);
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                Logger.getLogger(SocketServer.class.getName()).log( Level.SEVERE, "Unable to accept the connection", exc);
            }
        });
    }

    private void readWrite(AsynchronousSocketChannel channel, int clientNumber) {
        final ByteBuffer buf = ByteBuffer.allocate(4096);
        channel.read(buf, READ_TIME_OUT, TimeUnit.MILLISECONDS, channel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel attachment) {
                try {
                    buf.flip();
                    int limits = buf.limit();
                    byte bytes[] = new byte[limits];
                    buf.get(bytes, 0, limits);
                    Charset cs = Charset.forName("UTF-8");
                    String command = new String(bytes, cs);
                    String msg;
                    switch (command.toUpperCase()) {
                        case "WHO":
                            msg = "Total number of clients:" + clientNumber;
                            break;
                        case "WHERE":
                            msg = "Server id:" + id;
                            break;
                        case "WHY":
                            msg = "42";
                            break;
                        case "BYE":
                            channel.close();
                            removeClient();
                            msg = "bye";
                            break;
                        default:
                            msg = "Command not found";

                    }
                    attachment.write(ByteBuffer.wrap(msg.getBytes()));

                } catch (IOException e) {
                    Logger.getLogger(SocketServer.class.getName()).log( Level.SEVERE, "Error ", e);
                }
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                System.out.println("read write failed");
                Logger.getLogger(SocketServer.class.getName()).log( Level.SEVERE, "Unable to read from channel ", exc);
            }
        });
    }

    private void removeClient() {
        --clientNumber;
    }

    public static void main(String[] args) {
        try {
            new SocketServer("127.0.0.1", 3575);
            for( ; ; ) {
                Thread.sleep(10*1000);
            }

        } catch (Exception ex) {
            Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
