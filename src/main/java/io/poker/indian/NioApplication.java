package io.poker.indian;

import io.poker.indian.server.NioServer;
import java.io.IOException;

public class NioApplication {

  public static void main(String[] args) throws IOException {
    var server = new NioServer(8000);
    new Thread(server).start();
  }
}
