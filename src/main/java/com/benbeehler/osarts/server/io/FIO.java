package com.benbeehler.osarts.server.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class FIO {

	@SuppressWarnings("resource")
	public static String readF(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		
		String line;
		while((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("resource")
	public static void write(File file, String string) throws IOException {
		PrintStream ps = new PrintStream(file);
		ps.println(string);
	}
}
