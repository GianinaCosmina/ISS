package em.network.utils;


import em.network.rpcprotocol.ClientRpcWorker;
import em.services.Service;

import java.net.Socket;

public class TARpcConcurrentServer extends AbsConcurrentServer {
    private Service taServer;

    public TARpcConcurrentServer(int port, Service taServer) {
        super(port);
        this.taServer = taServer;
        System.out.println("Employee monitoring - TARpcConcurrentServer");
    }

    @Override
    protected Thread createWorker(Socket client) {
        ClientRpcWorker worker = new ClientRpcWorker(taServer, client);

        Thread tw = new Thread(worker);
        return tw;
    }

    @Override
    public void stop() {
        System.out.println("Stopping services ...");
    }
}
