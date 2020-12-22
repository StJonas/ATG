

public class Parser {
	public static final int _EOF = 0;
	public static final int _initials = 1;
	public static final int _string = 2;
	public static final int _date = 3;
	public static final int _time = 4;
	public static final int _kommazahl = 5;
	public static final int _id = 6;
	public static final int maxT = 16;

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
		Expect(11);
		int tasks = 0; 
		while (la.kind == 6) {
			Task();
			tasks++; 
		}
		Expect(12);
		int book = 0; 
		while (la.kind == 6) {
			Booking();
			book++; 
		}
		println(book + " Bookings."); 
		println(devs + " Entwickler/innen."); 
		println(tasks + " Tasks."); 
		
	}

	void Developer() {
		Initial();
		Name();
	}

	void Task() {
		Id();
		String status = Status();
		RemainingEffort();
		Description();
	}

	void Booking() {
		Id();
		Inhalt();
		while (la.kind == 3) {
			Inhalt();
		}
	}

	void Initial() {
		Expect(1);
	}

	void Name() {
		Expect(2);
	}

	void Id() {
		Expect(6);
	}

	String  Status() {
		String  status;
		status="open"; 
		if (la.kind == 13 || la.kind == 14 || la.kind == 15) {
			if (la.kind == 13) {
				Get();
				status="open"; 
			} else if (la.kind == 14) {
				Get();
				status="active"; 
			} else {
				Get();
				status="done"; 
			}
		}
		return status;
	}

	void RemainingEffort() {
		Expect(5);
	}

	void Description() {
		Expect(2);
	}

	void Inhalt() {
		Date();
		Kommazahl();
		WorkDone();
		while (la.kind == 1) {
			WorkDone();
		}
	}

	void Date() {
		Expect(3);
	}

	void Kommazahl() {
		Expect(5);
	}

	void WorkDone() {
		Initial();
		Kommazahl();
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		ScrumTaskboard();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x}

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
			case 5: s = "kommazahl expected"; break;
			case 6: s = "id expected"; break;
			case 7: s = "\"Export\" expected"; break;
			case 8: s = "\"of\" expected"; break;
			case 9: s = "\"Taskboard\" expected"; break;
			case 10: s = "\"Developers\" expected"; break;
			case 11: s = "\"Tasks\" expected"; break;
			case 12: s = "\"Bookings\" expected"; break;
			case 13: s = "\"open\" expected"; break;
			case 14: s = "\"active\" expected"; break;
			case 15: s = "\"done\" expected"; break;
			case 16: s = "??? expected"; break;
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
