package io.poker.indian.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

@Data
@Slf4j
public class NioServer implements Runnable {

  private ByteBuffer buf = ByteBuffer.allocate(256);
  private Selector selector;
  private ServerSocketChannel serverSocketChannel;
  private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to NioServer!\n".getBytes());

  public NioServer(int port) throws IOException {
    BasicConfigurator.configure();
    this.serverSocketChannel = ServerSocketChannel.open();
    this.selector = Selector.open();

    this.serverSocketChannel
        .bind(new InetSocketAddress(port))
        .configureBlocking(false)
        .register(selector, SelectionKey.OP_ACCEPT);
  }

  @Override
  public void run() {
    try {
      runServer();
    } catch (IOException e) {
      log.error("Server Error :: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void runServer() throws IOException {
    Iterator<SelectionKey> iter;
    SelectionKey key;
    while (serverSocketChannel.isOpen()) {
      selector.select();
      iter = this.selector.selectedKeys().iterator();
      while (iter.hasNext()) {
        key = iter.next();
        iter.remove();

        if (key.isAcceptable()) {
          this.handleAccept(key);
        }
        if (key.isReadable()) {
          this.handleRead(key);
        }
      }
    }
  }

  private void handleAccept(SelectionKey key) throws IOException {
    SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
    String address =
        socketChannel.socket().getInetAddress().toString() + ":" + socketChannel.socket().getPort();
    socketChannel.configureBlocking(false);
    socketChannel.register(selector, SelectionKey.OP_READ, address);
    socketChannel.write(welcomeBuf);
    welcomeBuf.rewind();
    log.info("Server :: accepted connection from: " + address);
  }

  private void handleRead(SelectionKey key) throws IOException {
    SocketChannel socketChannel = (SocketChannel) key.channel();
    StringBuilder sb = new StringBuilder();

    buf.clear();
    int read;
    while ((read = socketChannel.read(buf)) > 0) {
      buf.flip();
      byte[] bytes = new byte[buf.limit()];
      buf.get(bytes);
      sb.append(new String(bytes));
      buf.clear();
    }
    String message;
    if (read < 0) {
      message = key.attachment() + " left the chat.\n";
      socketChannel.close();
    } else {
      message = key.attachment() + ": " + sb.toString();
    }

    log.info("Server :: " + message);
    broadcast(message);
  }

  private void broadcast(String msg) throws IOException {
    ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
    for (SelectionKey key : selector.keys()) {
      if (key.isValid() && key.channel() instanceof SocketChannel) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        String address =
            socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, address);
        socketChannel.write(msgBuf);
        msgBuf.rewind();
      }
    }
  }
}
