package j3270;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Test {

	public static void main(String args[])
			throws InterruptedException, UnknownHostException, IOException, TimeoutException {

		String destinationTerminal = "016";
		String originTerminal = "017";

		Emulator session = new Emulator(999);

		session.start();
		session.connect("EXLAQA");
		session.printScreen();

		session.trySendTextToField("call xxc870", 20, 9);
		session.printScreen();
		session.sendEnter();

		session.trySendTextToField("1", 23, 10);
		session.printScreen();
		session.printScreen();
		session.sendEnter();

		session.trySendTextToField("18", 23, 10);
		session.trySendTextToField("test", 23, 23);
		session.trySendTextToField(originTerminal, 23, 49);
		session.printScreen();
		session.sendEnter();

		session.sendPF(16);
		session.printScreen();

		String trailer = addSetupRecord(session, destinationTerminal, "2");
		session.printScreen();
		session.sendF(1);
		session.printScreen();

		enterTrailerOption(session, trailer, "P");
		session.sendEnter();
		session.printScreen();

		session.trySendTextToField("Danny", 10, 37);
		session.sendEnter();
		session.trySendTextToField("1", 12, 24);
		session.sendEnter();
		session.printScreen();

		session.sendEnter();
		session.printScreen();
		session.trySendTextToField(destinationTerminal, 11, 70);
		session.printScreen();

		session.sendEnter();

		session.printScreen();
		enterTrailerOption(session, trailer, "M");
		session.printScreen();
		
	    session.sendEnter();
        session.printScreen();
        session.trySendTextToField("2", 3, 69);
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        session.sendF(3);
        session.printScreen();
        enterTrailerOption(session, trailer, "O");
        
        session.sendEnter();
        session.printScreen();
        session.trySendTextToField("EXTRA", 8, 35);
        session.printScreen();
        session.trySendTextToField("N", 12, 56);
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        session.trySendTextToField("Y", 5, 44);
        session.printScreen();
        session.sendEnter();
        session.sendF(1);
        session.printScreen();
        session.sendF(1);
        session.printScreen();
        session.trySendTextToField("018", 23, 10);
        session.trySendTextToField("test", 23, 23);
        session.trySendTextToField(destinationTerminal, 23, 49)  ;  
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        enterTrailerOption(session, trailer, "A");
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        String string = new String(session.readTextAtPosition(5, 40, 50).replace(":", ""));
        System.out.println(string);
        Integer integer = Integer.valueOf(string.trim()) + 10;
        String newTime = String.valueOf(integer);
        session.trySendTextToField(newTime, 8, 34);
        session.printScreen();
        session.sendEnter();
        session.printScreen();
        session.sendEnter();
        session.printScreen();

		session.endSession();

	}

	public static String addSetupRecord(Emulator session, String destinationTerminal, String trailerStartsWith) {

		int i = 1;
		int j = 9;

		String trailer = null;

		while (i < 100) {

			session.trySendTextToField(destinationTerminal, 9, 12);
			session.trySendTextToField("?", 12, 12);
			session.printScreen();
			session.sendEnter();

			session.trySendTextToField("MTY", 6, 29);
			session.printScreen();
			session.sendEnter();

			session.trySendTextToField(trailerStartsWith, 4, 54);
			session.printScreen();
			session.sendEnter();

			if (session.readTextAtPosition(j, 32, 6).contains(trailerStartsWith)) {
				session.trySendTextToField("1", j, 28);
				session.printScreen();
				session.sendEnter();
				trailer = session.readTextAtPosition(12, 12, 6);
				session.printScreen();
				session.sendEnter();
				if (session.getScreen().contains("Setup record")) {
					session.printScreen();
					System.out.println("trailer found: " + trailer);
					break;
				}
			} else if (session.readTextAtPosition(j, 32, 6).contains("      ")) {
				if (session.readTextAtPosition(21, 55, 4).contains("More")) {
					session.pageDown();
					j = 8;
				} else {
					System.out.println("no trailers found");
					session.endSession();
				}
			} else {
				System.out.println("no trailers found");
				session.endSession();
			}
			j += 1;
		}
		return trailer;
	}

	public static void enterTrailerOption(Emulator session, String trailer, String option) throws InterruptedException {
		int j = 6;
		int i = 1;
		while (i < 999) {
			if (!(new String(session.readTextAtPosition(j, 17, 6))).contains(("      "))) {
//				Thread.sleep(500);
				String actualValue = session.readTextAtPosition(j, 17, 6);
//				System.out.println(actualValue);
				if (actualValue.contains(trailer)) {
					System.out.println("trailer found!");
					session.trySendTextToField(option, j, 2);
					break;
				}
				j += 1;

			} else if (session.readTextAtPosition(20, 73, 4).contains("More")) {
				session.pageDown();
				j = 6;
			} else if (session.readTextAtPosition(20, 73, 6).contains("Bottom")) {
				System.out.println("Trailer not found.");
				break;
			}
		}
	}

}