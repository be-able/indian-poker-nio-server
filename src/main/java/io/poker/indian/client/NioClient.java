package io.poker.indian.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

@Slf4j
public class NioClient implements Runnable {

  private final InetSocketAddress addr;
  @Getter
  private static final List<String> recentMessages = new ArrayList<>();

  public NioClient(int port) {
    BasicConfigurator.configure();
    this.addr = new InetSocketAddress("localhost", port);
  }

  public void run() {
    log.info("Client :: Start");
    try (Selector selector = Selector.open(); SocketChannel socketChannel = SocketChannel.open()) {
      socketChannel.configureBlocking(false);
      socketChannel.connect(addr);
      socketChannel.register(selector, SelectionKey.OP_CONNECT);

      while (!Thread.currentThread().isInterrupted() && socketChannel.isOpen()) {
        if (selector.select() > 0) {
          handleSelectedKeys(selector);
        }
        Thread.sleep(1000);
      }
    } catch (IOException | InterruptedException e) {
      log.error("Client Error :: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void handleSelectedKeys(Selector selector) throws IOException, InterruptedException {
    Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
    while (itr.hasNext()) {
      SelectionKey key = itr.next();
      if (key.isAcceptable()) {
        log.info("Client :: Acceptable is for Server");
      } else if (key.isConnectable()) {
        connectSocket(key);
      } else if (key.isReadable()) {
        log.info("Client :: Readable is activated");
        readSocket(key);
      } else if (key.isWritable()) {
        writeSocket(key);
      }
      itr.remove();
    }
  }

  /**
   * key.isConnectable()일때 key의 소켓을 서버에 연결하고 소켓모드를 변경한다.
   */
  private void connectSocket(SelectionKey key) throws IOException {
    if (((SocketChannel) key.channel()).finishConnect()) {
      key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT);
      key.interestOps(key.interestOps() | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
    }
  }

  //key.isWritable()일때 데이터를 서버에 보낸다.
  private void writeSocket(SelectionKey key) throws IOException, InterruptedException {
    String[] messages = {"One", "Two", "Three", "Four", "DONE"};
    Random random = new Random();

    ByteBuffer buf = ByteBuffer.allocate(100);
    SocketChannel socketChannel = (SocketChannel) key.channel();

    for (String message : messages) {
      buf.clear();
      buf.put(message.getBytes());
      buf.flip();
      socketChannel.write(buf);
      log.info("Client :: write :: " + buf);
      Thread.sleep(random.nextInt(3000));
    }
  }

  private void readSocket(SelectionKey key) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(100);
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
      message = "left the chat.\n";
      socketChannel.close();
    } else {
      message = sb.toString();
    }

    log.info("Client :: msg :: " + message);
    recentMessages.add(message);
  }
}
