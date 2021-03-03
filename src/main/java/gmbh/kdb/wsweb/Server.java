package gmbh.kdb.wsweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Server implements Runnable {

    protected Socket request;
    protected String[] nodes;

    public Server(Socket request, String[] nodes) {
        this.request = request;
        this.nodes = nodes;
    }

    public static void main(String[] args) {
        String[] hosts = null;

        if (args.length > 0) {
            hosts = args[0].split(",");

            System.out.println("Using the followings cluster hosts:");
            Arrays.stream(hosts).forEach(System.out::println);
        } else {
            System.out.println("Running in standalone mode");
        }

        try (var sock = new ServerSocket(37766)) {

            System.out.println("Server is running on port 37766");

            while (true) {
                var server = new Server(sock.accept(), hosts);
                new Thread(server).start();
            }

        } catch (Exception err) {
            System.err.printf("Server failed = %s", err.getMessage());
        }
    }

    @Override
    public void run() {
        try (var in = new BufferedReader(new InputStreamReader(this.request.getInputStream()))) {
            var out = new PrintWriter(this.request.getOutputStream());

            // Parse the header
            var header = in.readLine();
            var tokenizer = new StringTokenizer(header);
            tokenizer.nextToken();
            var path = tokenizer.nextToken().toLowerCase();

            System.out.printf("-> %s\n", path);

            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/plain");
            out.println();

            this.printHostInformation(out);

            if (!path.equals("/single") && null != this.nodes && this.nodes.length > 0) {
                out.println();
                this.printNodeInformation(out);
            }

            out.flush();

        } catch (Exception e) {
            System.err.printf("Error = %s\n", e.getMessage());
        }
    }

    protected void printNodeInformation(PrintWriter out) throws Exception {

        Arrays.stream(this.nodes)
                .forEach(node -> {
                    var addr = "http://" + node + ":37766/single";

                    System.out.printf("Requesting %s ...\n", addr);

                    try {
                        var client = HttpClient.newHttpClient();
                        var req = HttpRequest.newBuilder(
                                URI.create(addr)
                        ).build();

                        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

                        out.printf("Node %s:\n%s\n", node, res.body());

                    } catch (Exception ex) {
                        System.err.printf("Failed to request %s", addr);
                    }
                });

    }

    protected void printHostInformation(PrintWriter body) throws IOException {
        var localhost = InetAddress.getLocalHost();

        body.printf("Host name: %s\n", localhost.getHostName());
        body.printf("Host address: %s\n", localhost.getHostAddress());
    }

}
