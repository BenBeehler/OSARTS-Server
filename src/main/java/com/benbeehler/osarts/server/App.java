package com.benbeehler.osarts.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

import org.json.JSONException;
import org.json.JSONObject;

import com.benbeehler.osarts.server.io.FIO;

public class App {
	
	private static final File configf = new File("./config.json");
	private static JSONObject config;
	private static int port = 80;
	private static String mount = "/";
	
	private static ServerSocket server;
	
	private static String fbuffer = "";
	private static String fpath;
	
	public static void main(String[] args) {
		if(!configf.exists()) {
			try {
				configf.createNewFile();
				
				config = new JSONObject("{}");
				config.put("system.port", port);
				config.put("system.mountDirectory", "./");
				
				FIO.write(configf, config.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			config = new JSONObject(FIO.readF(configf));
			
			port = config.getInt("system.port");
			mount = config.getString("system.mountDirectory");
			
			server = new ServerSocket(port);
			System.out.println("Server running on " + port);
			System.out.println("Resources will mount on " + mount);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		listen();
	}
	
	private static void listen() {
		new Thread(() -> {
			while(true) {
				try {
					Socket socket = server.accept();
					DataInputStream stream = new DataInputStream(socket.getInputStream());
					
					String read = stream.readUTF();
					determine(read);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private static void determine(String string) {
		if(string.startsWith("SIGNATURE_NEWFILE")) {
			string = string.replaceFirst("SIGNATURE_NEWFILE", "").trim();
			
			fpath = string;
		} else if(string.startsWith("SIGNATURE_APPEND")) {
			string = string.replaceFirst("SIGNATURE_APPEND", "").trim();
			
			fbuffer = fbuffer + "\n" + string;
		} else if(string.equals("SIGNATURE_ENDFILE")) {
			String fullPath = fpath;
			
			if(fullPath.startsWith("."))
				fullPath = fullPath.replaceFirst(".", "").trim();
			
			if(!fullPath.startsWith("/"))
				fullPath = "/" + fullPath;
			
			File file = new File(mount + fullPath);
			System.out.println("Finalizing: " + mount + fullPath);
			
			try {
				if(!file.exists()) {
					File parent = file.getParentFile();
					parent.mkdirs();
					
					file.createNewFile();
				}
				FIO.write(file, fbuffer);
				fbuffer = "";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
