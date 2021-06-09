package io.poker.indian;

import io.poker.indian.server.NioServer;
import java.io.IOException;

public class NioApplication {

  public static void main(String[] args) throws IOException {
    var server = new NioServer(8000);
    (new Thread(server)).start();
    new Thread(() -> {
      System.out.println("Sleeping... " + Thread.currentThread().getName());
      try {
        Thread.sleep(1000000000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
    System.out.println("hyob babo");
  }
}
