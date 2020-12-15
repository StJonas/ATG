

public class Parser {
	public static final int _EOF = 0;
	public static final int _initials = 1;
	public static final int _string = 2;
	public static final int _date = 3;
	public static final int _time = 4;
	public static final int _bezeichnung = 5;
	public static final int _kommazahl = 6;
	public static final int maxT = 12;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	void println(String string) { 
		System.out.println(string);
	}
		
	
/*----------------------------------------------------------
  --- SCANNER
  ----------------------------------------------------------*/



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void ScrumTaskboard() {
		println("Entwickler/innen:"); 
		
		Expect(7);
		Expect(8);
		Expect(9);
		Expect(3);
		Expect(4);
		Expect(10);
		int devs = 0; 
		while (la.kind == 1) {
			Developer();
			devs++; 
		}
		println(""); 
		println("Das sind " + devs + " Entwickler/innen."); 
		println(""); 
		
		Expect(11);
		int book = 0; 
		while (la.kind == 5) {
			Booking();
			book++; 
		}
		println(""); 
		println("Das sind " + book + " Bookings."); 
		println(""); 
		
		
	}

	void Developer() {
		String initials = Initials();
		String name = Name();
		println("    " + name + " ["+ initials +"]"); 
	}

	void Booking() {
		String bezeichnung = Bezeichnung();
		println(bezeichnung);  
		Inhalt();
		while (la.kind == 3) {
			Inhalt();
		}
	}

	String  Initials() {
		String  initials;
		Expect(1);
		initials = t.val; 
		return initials;
	}

	String  Name() {
		String  name;
		Expect(2);
		name = t.val; 
		return name;
	}

	String  Bezeichnung() {
		String  bezeichnung;
		Expect(5);
		bezeichnung = t.val; 
		return bezeichnung;
	}

	void Inhalt() {
		String date = Date();
		float kommazahl1 = Kommazahl();
		println("	" + date + ": " + kommazahl1);  
		WorkDone();
		while (la.kind == 1) {
			WorkDone();
		}
	}

	String  Date() {
		String  date;
		Expect(3);
		date = t.val; 
		return date;
	}

	float  Kommazahl() {
		float  kommazahl;
		Expect(6);
		kommazahl = Float.valueOf(t.val); 
		return kommazahl;
	}

	void WorkDone() {
		String init = Initials();
		float kommazahl2 = Kommazahl();
		println("		" + init + " - " + kommazahl2);
		
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		ScrumTaskboard();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "initials expected"; break;
			case 2: s = "string expected"; break;
			case 3: s = "date expected"; break;
			case 4: s = "time expected"; break;
			case 5: s = "bezeichnung expected"; break;
			case 6: s = "kommazahl expected"; break;
			case 7: s = "\"Export\" expected"; break;
			case 8: s = "\"of\" expected"; break;
			case 9: s = "\"Taskboard\" expected"; break;
			case 10: s = "\"Developers\" expected"; break;
			case 11: s = "\"Bookings\" expected"; break;
			case 12: s = "??? expected"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
