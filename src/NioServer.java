import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * ${DESCRIPTION}
 *
 * @author 温柔一刀
 * @create 2018-10-28 20:51
 **/
public class NioServer {
    //缓冲区的长度
    private static final int BUFSIZE = 256;
    //select方法等待信道准备好的最长时间
    private static final int TIMEOUT = 3000;

    public static void main(String[] args) throws IOException {
        //创建选择器
        Selector selector = Selector.open();
        //实例化一个信道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        int port = 9100;
        //将该信道绑定到指定端口
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 设置非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //将选择器注册到各个信道
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //不断轮询select方法，获取准备好的信道所关联的Key集
        while (true) {

            int readChanelsCoount = selector.select(TIMEOUT);
            if (readChanelsCoount == 0) {
                continue;
            }

            //获取准备好的信道所关联的Key集合的iterator实例
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
            //创建一个实现了协议接口的对象
            TCPProtocol protocol = new SelectorProtocol(BUFSIZE);
            //循环取得集合中的每个键值
            while (selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();
                //服务端信道I/O操作为accept
                if (key.isAcceptable()) {
                    protocol.handleAccept(key);
                    //服务端信道I/O操作为read
                } else if (key.isReadable()) {
                    protocol.handleRead(key);

                    //服务端信道I/O操作为write
                } else if (key.isWritable()) {
                    protocol.handleWrite(key);

                }
                //这里需要手动从键集中移除当前的key
                selectionKeyIterator.remove();
            }


        }
    }

}
