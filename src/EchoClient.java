import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Steven
 */
public class EchoClient {

    public EchoClient( String host, int port, final String message, final AtomicInteger messageWritten, final AtomicInteger messageRead ) throws IOException {
        AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();
        sockChannel.connect( new InetSocketAddress(host, port), sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel >() {
            @Override
            public void completed(Void result, AsynchronousSocketChannel channel ) {
                startRead( channel,messageRead );
                startWrite( channel, message, messageWritten );
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println( "fail to connect to server");
            }

        });
    }

    private void startRead( final AsynchronousSocketChannel sockChannel, final AtomicInteger messageRead ) {
        final ByteBuffer buf = ByteBuffer.allocate(2048);

        sockChannel.read( buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>(){

            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                messageRead.getAndIncrement();
                System.out.println( "Read message:" + new String( buf.array()) );
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println( "fail to read message from server");
            }

        });

    }
    private void startWrite( final AsynchronousSocketChannel sockChannel, final String message, final AtomicInteger messageWritten ) {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        buf.put(message.getBytes());
        buf.flip();
        messageWritten.getAndIncrement();
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel ) {
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println( "Fail to write the message to server");
            }
        });
    }

    public static void main( String...args ) {
        try {
            AtomicInteger messageWritten = new AtomicInteger( 0 );
            AtomicInteger messageRead = new AtomicInteger( 0 );
            new EchoClient( "127.0.0.1", 3575, "who", messageWritten, messageRead );
            new EchoClient( "127.0.0.1", 3575, "where", messageWritten, messageRead );
            new EchoClient( "127.0.0.1", 3575, "why", messageWritten, messageRead );
            new EchoClient( "127.0.0.1", 3575, "who", messageWritten, messageRead );
           while( messageRead.get() != 1) {
                Thread.sleep( 1000 );
                System.out.println( "message write:" + messageWritten );
                System.out.println( "message read:" + messageRead );
            }
        } catch (Exception ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}