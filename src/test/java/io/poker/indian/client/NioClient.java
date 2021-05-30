package io.poker.indian.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

public class NioClient implements Runnable {

  private InetSocketAddress addr;

  public NioClient(int port) {
    this.addr = new InetSocketAddress("localhost", port);
  }

  public void run() {
    System.out.println("start");
    try (Selector selector = Selector.open();            //셀렉터 오픈
        SocketChannel socketChannel = SocketChannel.open();) {  //소켓 오픈
      socketChannel.configureBlocking(false);            //소켓채널 비차단모드로 설정
      socketChannel.connect(addr);                //서버주소 지정
      socketChannel.register(selector, SelectionKey.OP_CONNECT);  //연결모드로 설정

      while (!Thread.currentThread().isInterrupted() && socketChannel.isOpen()) {
        if (selector.select() > 0) {              //입출력 소켓이 있는 경우
          handleSelectedKeys(selector);            //선택된 소켓 처리
        }
        Thread.sleep(1000);                  //1초에 한번씩 체크
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //selectedKeys에 있는 모든 key의 상태를 보고 그에 따른 처리를 한다.
  private void handleSelectedKeys(Selector selector) throws IOException, InterruptedException {
    Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
    while (itr.hasNext()) {
      SelectionKey key = (SelectionKey) itr.next();
      if (key.isAcceptable()) {
        System.out.format("Acceptable is for Server.%n");
      } else if (key.isConnectable()) {              //서버에 연결이 되었을 때
        connectSocket(key);
      } else if (key.isReadable()) {
        System.out.format("Readable is activated.%n");
      } else if (key.isWritable()) {                //데이터를 보낼 때가 된 경우
        writeSocket(key);
      }
      itr.remove();
    }
  }

  //key.isConnectable()일때 key의 소켓을 서버에 연결하고 소켓모드를 OP_WRITE로 바꾼다.
  private void connectSocket(SelectionKey key) throws IOException {
    if (((SocketChannel) key.channel()).finishConnect()) {
      key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT);
      key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }
  }

  //key.isWritable()일때 데이터를 서버에 보낸다.
  private void writeSocket(SelectionKey key) throws IOException, InterruptedException {
    String messages[] = {"One", "Two", "Three", "Four", "DONE"};
    Random random = new Random();

    ByteBuffer buf = ByteBuffer.allocate(80);
    SocketChannel socketChannel = (SocketChannel) key.channel();

    for (int i = 0; i < messages.length; i++) {
      buf.clear();              //buf초기화
      buf.put(messages[i].getBytes());    //buf채움
      buf.flip();                //limit=position, position=0
      socketChannel.write(buf);        //소켓채널에 데이터를 전송
      System.out.println("write : " + buf);
      Thread.sleep(random.nextInt(3000));    //최대 3초까지 멈춤
    }
  }
}
