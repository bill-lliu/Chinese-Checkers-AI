import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
	private String name = "";
	private String status = "";
	private Socket client;
	public BufferedReader input;
	private PrintWriter output; //assign printwriter to network stream

	User(Socket client) {
		this.client = client;
		try {  //assign all connections to client
			this.output = new PrintWriter(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void send(String msg) {
		output.println(msg);
		output.flush();
	}

	String getName() {
		return this.name;
	}

	void setName(String name) {
		this.name = name;
	}

	Socket getSocket() {
		return this.client;
	}

	String getStatus() {
		return this.status;
	}

	void setStatus(String status) {
		if (this.status.equals("")) {
			this.status = "Online";
			return;
		}
		this.status = status;
	}

	void close() {
		try {
			client.close();
			output.close();
			input.close();
		}
		catch (Exception e) {
			System.out.println("Failed to close socket");
		}
	}
}
