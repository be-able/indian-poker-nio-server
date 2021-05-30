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

@Data
public class NioServer implements Runnable {

  private ByteBuffer buf = ByteBuffer.allocate(256);
  private int port;
  private Selector selector;
  private ServerSocketChannel serverSocketChannel;
  private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to NioServer!\n".getBytes());

  public NioServer(int port) throws IOException {
    this.port = port;
    this.serverSocketChannel = ServerSocketChannel.open();
    this.serverSocketChannel.bind(new InetSocketAddress(port));
    this.serverSocketChannel.configureBlocking(false);
    this.selector = Selector.open();
    this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
  }

  @Override
  public void run() {
    try {
      System.out.println("Server starting on port " + this.port);

      Iterator<SelectionKey> iter;
      SelectionKey key;
      while (this.serverSocketChannel.isOpen()) {
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
    } catch (IOException e) {
      System.out.println("IOException, server of port " + this.port + " terminating. Stack trace:");
      e.printStackTrace();
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
    System.out.println("accepted connection from: " + address);
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
    String msg;
    if (read < 0) {
      msg = key.attachment() + " left the chat.\n";
      socketChannel.close();
    } else {
      msg = key.attachment() + ": " + sb.toString();
    }

    System.out.println(msg);
    broadcast(msg);
  }

  private void broadcast(String msg) throws IOException {
    ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
    for (SelectionKey key : selector.keys()) {
      if (key.isValid() && key.channel() instanceof SocketChannel) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(msgBuf);
        msgBuf.rewind();
      }
    }
  }
}
