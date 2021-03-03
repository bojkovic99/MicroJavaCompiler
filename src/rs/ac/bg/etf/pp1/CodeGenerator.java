package rs.ac.bg.etf.pp1;

import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	private List<Integer> operacije = new ArrayList<>();
//	private List<Obj> indeksiranjeStek = new ArrayList<>();
	private Obj objekat;
	private boolean indeksiranjeNiza = false;
	private boolean indeksiranje = false;
	private boolean otvorenaZagrada = false;
	private int indeks = 0;
	private int adresa = 0;
	private int adresa2 = 0;
	private int nivo = 0;
	private boolean JesteNiz = false;
	private boolean definisanMain = false;
//	private int adresaElse;
	private int tacanIf;
//	private int poslednjaAdresa = 0;
	private int doWhileNivo = 0;

	private List<Integer> poslednje = new ArrayList<>();
	private List<Integer> relop = new ArrayList<>();
	private List<Integer> listAdresa = new ArrayList<>();
	private List<Integer> listaZaElse = new ArrayList<>();
	private List<Integer> poslednjaLista = new ArrayList<>();
	private List<Integer> tacniUslovi = new ArrayList<>();
	private List<Integer> pocetnoDo = new ArrayList<>();
	private List<Integer> breakAdrese = new ArrayList<>();
	private List<Integer> brBreak = new ArrayList<>();
	private List<Integer> contAdrese = new ArrayList<>();
	private List<Integer> brCont = new ArrayList<>();
	private List<Integer> poslednjeAdr = new ArrayList<>();
	private List<Integer> brPosl = new ArrayList<>();
	private List<Integer> PoslednjalistaZaElse = new ArrayList<>();
	private List<Integer> brZaElse = new ArrayList<>();
	private ArrayList<Boolean> otvoreneZagradeMinus = new ArrayList<>();

	static final int minus = Code.sub, plus = Code.add, puta = Code.mul, podeljeno = Code.div, procenat = Code.rem,
			negacija = 6, ozag = 7, zzag = 8, simbol = 100, uglasta = 9;

	static final int manjejednako = 3, manje = 2, vecejednako = 5, vece = 4, razlicito = 1, jendakojednako = 0;
	public static int inverse[] = { razlicito, jendakojednako, vecejednako, vece, manjejednako, manje };

	public int getMainPc() {
		return mainPc;
	}

	int stampanje = -1;

	public void visit(PostojiNumerickaKonstanta konst) {
		stampanje = konst.getBroj2();
	}

	public void visit(PrintNaredba print) {
		if (print.getExpr().struct == Tab.intType || print.getExpr().struct == Tabela.boolType) {
			if (stampanje != -1) {
				Code.loadConst(stampanje);
				stampanje = -1;
			} else
				Code.loadConst(5);
			Code.put(Code.print);
		} else {
			if (stampanje != -1) {
				Code.loadConst(stampanje);
				stampanje = -1;
			} else
				Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}

	public void visit(ReadNaredba read) {
		if (read.getDesignator().obj.getType() == Tab.intType
				|| read.getDesignator().obj.getType() == Tabela.boolType) {

			Code.put(Code.read);

		} else {

			Code.put(Code.bread);

		}

		if (!JesteNiz) {
			Code.store(read.getDesignator().obj);
		} else {
			if (read.getDesignator().obj.getType() == Tab.intType
					|| read.getDesignator().obj.getType() == Tabela.boolType) {

				Code.put(Code.astore);

			} else {

				Code.put(Code.bastore);

			}
			JesteNiz = false;

		}
	}

	boolean bioMinus = false;

	public void visit(NumerickaKonstanta konst) {
		// JesteNiz = false;
		Obj con = new Obj(Obj.Con, "$", konst.struct);
		con.setLevel(0);
		con.setAdr(konst.getBroj());

		SyntaxNode parent = konst.getParent();

		if (detektovanMinus) {

			Code.put(Code.const_n);

		}
		if (parent.getParent().getParent().getClass() == TernarniDrugaOpcija.class) {

			Code.fixup(adresa);
		}

		Code.load(con);
		if (detektovanMinus) {

			Code.put(Code.sub);
			detektovanMinus = false;

		}
	}

	public void visit(ObicanBroj designator) {
		SyntaxNode parent = designator.getParent();

		if (designator.obj.getType() == Tab.intType && designator.obj.getKind() == 0) {
			Obj consta = new Obj(Obj.Con, "l", Tab.intType);
			consta.setLevel(0);
			consta.setAdr(designator.obj.getAdr());

//			if (parent.getParent().getParent().getParent().getParent().getClass() == NoviIzraz.class) {
//				otvorenaZagrada = true;
//				nivo++;
//				if (indeksiranjeNiza == false)
//					operacije.add(ozag);
//			}
			if (detektovanMinus) {
				Code.put(Code.const_n);
			}

			Code.load(consta);

			if (detektovanMinus) {
				Code.put(Code.sub);
				detektovanMinus = false;
			}

		} else if (designator.obj.getType() == Tab.charType && designator.obj.getKind() == 0) {

			Obj consta = Tab.find(designator.obj.getName());

			Code.load(consta);

		} else if (designator.obj.getType() == Tabela.boolType && designator.obj.getKind() == 0) {
			Obj consta = Tab.find(designator.obj.getName());
			Code.load(consta);
		}

		else if (parent.getClass() == PrintNaredba.class || parent.getClass() == FactorDesignator.class) {

//			if (parent.getParent().getParent().getParent().getParent().getClass() == NoviIzraz.class) {
//				otvorenaZagrada = true;
//				nivo++;
//				if (indeksiranjeNiza == false)
//					operacije.add(ozag);
//			}

			if (parent.getParent().getParent().getParent().getClass() == TernarniDrugaOpcija.class) {

				Code.fixup(adresa);

			}
			if (detektovanMinus) {
				Code.put(Code.const_n);
			}

			Code.load(designator.obj);

			if (detektovanMinus) {
				Code.put(Code.sub);
				detektovanMinus = false;
			}

		}

	}

	boolean pisi = false;

	public void visit(ElementNiza elem) {
		SyntaxNode parent = elem.getParent();
		operacije.add(uglasta);
		// System.out.println(" Dodao je uglastu! ");

		if (parent.getParent().getClass() == PrintNaredba.class
				|| parent.getParent().getClass() == FactorDesignator.class
				|| parent.getParent().getClass() == DodelaVrednostiDes.class
				|| parent.getParent().getClass() == UnarniMinusMinus.class
				|| parent.getParent().getClass() == UnarniPlusPlus.class
				|| parent.getParent().getClass() == ReadNaredba.class) {

			if (detektovanMinus) {
				Code.put(Code.const_n);

				nizMin.add(true);
				detektovanMinus = false;
				pisi = true;

			} else
				nizMin.add(false);

			Code.load(elem.obj);
			if (parent.getParent().getClass() == ReadNaredba.class) {
				JesteNiz = true;
			}

		}

	}

	public void visit(DesignatorNiz designator) {
		// JesteNiz = false;

		SyntaxNode parent = designator.getParent();

		if ((designator.obj.getType() == Tab.charType
				|| (designator.obj.getType().getKind() == 3 && designator.obj.getType().getElemType() == Tab.charType))
				&& parent.getClass() != DodelaVrednostiDes.class && parent.getClass() != UnarniPlusPlus.class
				&& parent.getClass() != UnarniMinusMinus.class && parent.getClass() != ReadNaredba.class) {
			naisaoNaUglastu();

			Code.put(Code.baload);
		} else if ((designator.obj.getType() == Tab.intType
				|| (designator.obj.getType().getKind() == 3 && designator.obj.getType().getElemType() == Tab.intType))
				&& parent.getClass() != DodelaVrednostiDes.class && parent.getClass() != UnarniPlusPlus.class
				&& parent.getClass() != UnarniMinusMinus.class && parent.getClass() != ReadNaredba.class) {
			naisaoNaUglastu();
			Code.put(Code.aload);

		}

		if (nizMin.get(nizMin.size() - 1)) {

			Code.put(Code.sub);
		}
		nizMin.remove(nizMin.size() - 1);

	}

	public void visit(ImePrograma ime) {
		// ord
		Obj objekat = Tab.find("ord");
		objekat.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.add);
		Code.put(Code.exit);
		Code.put(Code.return_);
		// chr
		Obj objekat2 = Tab.find("chr");
		objekat2.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.add);
		Code.put(Code.exit);
		Code.put(Code.return_);
		// len
		Obj objekat3 = Tab.find("len");
		objekat3.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		// Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.arraylength);
		Code.put(Code.exit);
		Code.put(Code.return_);

	}

////////////////// POZIVI FUNKCIJA //////////////////////////////
	public void visit(PomocniPostoji funcCall) {

		Obj functionObj = funcCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);

		Code.put2(offset);
	}

	public void visit(FunkcijaBezArgumenata procCall) {
		Obj functionObj = procCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if (procCall.getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}

	public void visit(FunkcijaSaArgumentima procCall) {

		Obj functionObj = procCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if (procCall.getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}

	public void visit(ImaPovratnuVrednost methodTypeName) {
		methodTypeName.obj.setAdr(Code.pc);

		SyntaxNode methodNode = methodTypeName.getParent();

		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());

	}

	public void visit(PovratnaVrVoid methodTypeName) {
		if ("main".equalsIgnoreCase(methodTypeName.getImeFje())) {
			definisanMain = true;
			mainPc = Code.pc;
		}

		methodTypeName.obj.setAdr(Code.pc);

		SyntaxNode methodNode = methodTypeName.getParent();

		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	}

	public boolean postojiMain() {
		return definisanMain;
	}

	public void visit(ZavrsenaFunkcija methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(ReturnNaredba ret) {
		Code.put(Code.exit);
		Code.put(Code.return_);

	}

//////////////////////////////// KONSTANTE /////////////////////////////////////////////
	public void visit(KarakterKonst konst) {
		SyntaxNode parent = konst.getParent();
		Obj con = Tab.insert(Obj.Con, "$", konst.struct);
		con.setLevel(0);
		con.setAdr(konst.getCC1().charAt(1));

		if (parent.getParent().getParent().getClass() == TernarniDrugaOpcija.class) {

			Code.fixup(adresa);
		}
		Code.load(con);
	}

	public void visit(BoolKonstanta konst) {
		SyntaxNode parent = konst.getParent();
		Obj con = Tab.insert(Obj.Con, "$", konst.struct);
		con.setLevel(0);
		if (konst.getBC1().equals("true")) {
			con.setAdr(1);
		} else
			con.setAdr(0);

		if (parent.getParent().getParent().getClass() == TernarniDrugaOpcija.class) {
			Code.fixup(adresa);
		}

		Code.load(con);
	}

	public void visit(UnarniMinusMinus izraz) {

		boolean dodato = false;
		if (izraz.getDesignator().getClass() == DesignatorNiz.class) {
			Code.put(Code.dup2);
			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}

			Code.put(Code.const_m1);
			Code.put(Code.add);
			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}

		} else {
			Code.load(izraz.getDesignator().obj);
			Code.put(Code.const_m1);
			Code.put(Code.add);
			Code.store(izraz.getDesignator().obj);

		}

		objekat = null;

	}

	public void visit(UnarniPlusPlus izraz) {
		boolean dodato = false;
		if (izraz.getDesignator().getClass() == DesignatorNiz.class) {

			Code.put(Code.dup2);

			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}

			Code.put(Code.const_1);
			Code.put(Code.add);
			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}

		} else {

			Code.load(izraz.getDesignator().obj);
			Code.put(Code.const_1);
			Code.put(Code.add);
			Code.store(izraz.getDesignator().obj);

		}

		objekat = null;

	}

	public void visit(DodelaVrednostiDes izraz) {
		if (izraz.getDesignator().getClass() == DesignatorNiz.class) {
			if (izraz.getDesignator().obj.getType() == Tab.charType
					|| (izraz.getDesignator().obj.getType().getKind() == 3
							&& izraz.getDesignator().obj.getType().getElemType() == Tab.charType)) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}

		} else
			Code.store(izraz.getDesignator().obj);
	}

	ArrayList<Integer> negacije = new ArrayList<>();
	ArrayList<Boolean> nizMin = new ArrayList<>();
	boolean detektovanMinus = false;
	boolean postojiLista = false;

	public void visit(PostojiMinus minusic) {
		detektovanMinus = true;
		bioMinus = true;

	}
/////////////////////// OPERACIJE //////////////////////////////////////////////////////////////////
	public void visit(ListaOperacijaPostoji izraz) {

		SyntaxNode parent = izraz.getParent();
		if (parent.getParent().getClass() == DesignatorNiz.class) {
			indeksiranje = true;
		}

		if (!otvorenaZagrada) {
			for (int i = operacije.size() - 1; i >= 0; i--) {
				if (operacije.get(i) == plus) {
					Code.put(Code.add);
					operacije.remove(i);
				} else if (operacije.get(i) == minus) {
					Code.put(Code.sub);
					operacije.remove(i);
				} else if (operacije.get(i) == negacija) {

					Code.put(Code.sub);
					operacije.remove(i);
				} else if (operacije.get(i) == ozag) {

					break;
				} else if (operacije.get(i) == uglasta) {

					break;
				}	else if (operacije.get(i) == puta) {
					break;
				} else if (operacije.get(i) == podeljeno) {
					break;
				} else if (operacije.get(i) == procenat) {
					break;
				}
			}

		}

	}

	public void visit(PostojiListaMulFact mul) {
		if (indeksiranjeNiza == false && otvorenaZagrada == false) {
			for (int i = operacije.size() - 1; i >= 0; i--) {
				if (operacije.get(i) == puta) {
					Code.put(Code.mul);
					operacije.remove(i);
				} else if (operacije.get(i) == podeljeno) {

					Code.put(Code.div);
					operacije.remove(i);
				} else if (operacije.get(i) == procenat) {
					Code.put(Code.rem);
					operacije.remove(i);
				} else if (operacije.get(i) == minus) {

					break;
				} else if (operacije.get(i) == plus) {

					break;
				} else if (operacije.get(i) == ozag) {

					break;
				} else if (operacije.get(i) == uglasta) {

					break;
				}
			}
		}
	}

	public void visit(Sabiranje sab) {

		if (indeksiranjeNiza == true) {
			poslednje.add(plus);

		} else {
			plusminus();

		}
		operacije.add(plus);

	}

	public void visit(Oduzimanje sab) {
		SyntaxNode parent = sab.getParent();
		if (indeksiranjeNiza == true) {
			poslednje.add(minus);

		} else {
			plusminus();
		}

		operacije.add(minus);

	}

	public void visit(Mnozenje sab) {
		if (indeksiranjeNiza == true) {
			poslednje.add(puta);

		} else {
			muldivrem();
		}
		operacije.add(puta);

	}

	public void visit(Deljenje sab) {
		if (indeksiranjeNiza == true) {
			poslednje.add(podeljeno);

		} else {
			muldivrem();
		}
		operacije.add(podeljeno);

	}

	public void visit(Ostatak sab) {
		if (indeksiranjeNiza == true) {
			poslednje.add(procenat);

		} else {
			muldivrem();
		}
		operacije.add(procenat);

	}

	public void visit(NewSimbol novi) {
		if (novi.getType().struct == Tab.intType || novi.getType().struct == Tabela.boolType) {
			Code.put(Code.newarray);
			// Code.put(Code.const_1);
			Code.loadConst(1);

		} else if (novi.getType().struct == Tab.charType) {
			Code.put(Code.newarray);
			// Obj obj = new Obj(Obj.Con, "kon", Tab.charType, 0,0);
			// Code.put(Code.const_n);
			Code.loadConst(0);

		}
	}

	public void visit(NoviIzraz2 izraz) { // System.out.println("zagrada " );

		if (detektovanMinus) {
			Code.put(Code.const_n);
			otvoreneZagradeMinus.add(true);
			detektovanMinus = false;
		} else
			otvoreneZagradeMinus.add(false);
		otvorenaZagrada = true;
		nivo++;
		operacije.add(ozag);
	}

	public void visit(NoviIzraz izraz) {
		ArrayList<Integer> zaBrisanje = new ArrayList<>();

		for (int i = operacije.size() - 1; i >= 0; i--) {
			if (operacije.get(i) == plus) {

				Code.put(Code.add);
				operacije.remove(i);
			} else if (operacije.get(i) == minus) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == podeljeno) {
				Code.put(Code.div);
				operacije.remove(i);
			} else if (operacije.get(i) == puta) {
				Code.put(Code.mul);
				operacije.remove(i);
			} else if (operacije.get(i) == procenat) {
				Code.put(Code.rem);
				operacije.remove(i);
			} else if (operacije.get(i) == negacija) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == ozag) {
				operacije.remove(i);
				if (otvoreneZagradeMinus.get(otvoreneZagradeMinus.size() - 1)) {
					Code.put(Code.sub);
				}
				otvoreneZagradeMinus.remove(otvoreneZagradeMinus.size() - 1);
				nivo--;
				if (nivo == 0)
					otvorenaZagrada = false;
				break;
			} else if (operacije.get(i) == uglasta) {
				break;
			}
		}

		for (Integer integer : zaBrisanje) {
			operacije.remove(zaBrisanje);
		}

	}

	public void visit(TesrnarniUslov uslov) {

		Code.put(Code.const_n);
		adresa = Code.pc + 1;
		Code.putFalseJump(Code.ne, 0);

	}

	public void visit(TernarniDrugaOpcija ter) {
		Code.fixup(adresa2);
	}

	public void visit(TernarniPrvaOpcija ter) {
		adresa2 = Code.pc + 1;
		Code.putJump(0);
	}

	public void muldivrem() {
		for (int i = operacije.size() - 1; i >= 0; i--) {
			if (operacije.get(i) == plus) {
				break;
			} else if (operacije.get(i) == minus) {
				break;
			} else if (operacije.get(i) == puta) {
				Code.put(Code.mul);
				operacije.remove(i);
			} else if (operacije.get(i) == podeljeno) {
				Code.put(Code.div);
				operacije.remove(i);
			} else if (operacije.get(i) == procenat) {
				Code.put(Code.rem);
				operacije.remove(i);
			} else if (operacije.get(i) == negacija) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == ozag) {

				break;
			} else if (operacije.get(i) == uglasta) {

				break;
			}
		}
	}

	public void plusminus() {
		for (int i = operacije.size() - 1; i >= 0; i--) {
			if (operacije.get(i) == plus) {
				Code.put(Code.add);

				operacije.remove(i);
			} else if (operacije.get(i) == minus) {

				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == puta) {
				Code.put(Code.mul);
				operacije.remove(i);
			} else if (operacije.get(i) == podeljeno) {
				Code.put(Code.div);
				operacije.remove(i);
			} else if (operacije.get(i) == procenat) {
				Code.put(Code.rem);
				operacije.remove(i);
			} else if (operacije.get(i) == negacija) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == ozag) {

				break;
			} else if (operacije.get(i) == uglasta) {

				break;
			}
		}
	}

	public void naisaoNaUglastu() {
		for (int i = operacije.size() - 1; i >= 0; i--) {
			if (operacije.get(i) == plus) {

				Code.put(Code.add);
				operacije.remove(i);
			} else if (operacije.get(i) == minus) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == podeljeno) {
				Code.put(Code.div);
				operacije.remove(i);
			} else if (operacije.get(i) == puta) {
				Code.put(Code.mul);
				operacije.remove(i);
			} else if (operacije.get(i) == procenat) {
				Code.put(Code.rem);
				operacije.remove(i);
			} else if (operacije.get(i) == negacija) {
				Code.put(Code.sub);
				operacije.remove(i);
			} else if (operacije.get(i) == ozag) {
				operacije.remove(i);
				if (otvoreneZagradeMinus.get(otvoreneZagradeMinus.size() - 1)) {
					Code.put(Code.sub);
				}
				otvoreneZagradeMinus.remove(otvoreneZagradeMinus.size() - 1);
				nivo--;
				if (nivo == 0)
					otvorenaZagrada = false;

				break;
			} else if (operacije.get(i) == uglasta) {
				operacije.remove(i);
				break;
			}
		}

	}
	////////////////////////////////////////////// CONDITION
	////////////////////////////////////////////// //////////////////////////////////////////////////////////////////

	public void visit(MinusJedanko rel) {
		relop.add(manjejednako);
	}

	public void visit(Malo rel) {
		relop.add(manje);
	}

	public void visit(AVeceJed rel) {
		relop.add(vecejednako);
	}

	public void visit(AVise rel) {
		relop.add(vece);
	}

	public void visit(Razlicito rel) {
		relop.add(razlicito);
	}

	public void visit(Isto rel) {
		relop.add(jendakojednako);
	}

	public void visit(CCondTerm term) {

		poslednjaLista = new ArrayList<>();
		if (listAdresa.size() > 0)
			listaZaElse.add(listAdresa.get(listAdresa.size() - 1));

		for (int i = 0; i < listAdresa.size(); i++) {
			Code.fixup(listAdresa.get(i));
			poslednjaLista.add(listAdresa.get(i));
		}

		listAdresa = new ArrayList<>();

	}

	public void visit(SecondOption op) {
		relop.add(razlicito);
		Code.put(Code.const_n);
		listAdresa.add(Code.pc + 1);
		Code.putFalseJump(Code.ne, 0);
	}

	public void visit(CondFactd conf) {
		// ako mu je treci parent PostojilistaCondTerm onda je || i onda onaj prethodni
		// treba da se obrise iz liste a on da se doda NA POCETAK OVOG CONDFACT A NE NA
		// KRAJ

//		System.out.println("Adresa za popravku !  " + Code.pc);

		listAdresa.add(Code.pc + 1);
		if (relop.size() > 0) {
			switch (relop.get(relop.size() - 1)) {
			case vece:

				Code.putFalseJump(Code.gt, 0);

				break;

			case manje:

				Code.putFalseJump(Code.lt, 0);

				break;

			case vecejednako:

				Code.putFalseJump(Code.ge, 0);

				break;
			case manjejednako:

				Code.putFalseJump(Code.le, 0);

				break;

			case razlicito:

				Code.putFalseJump(Code.ne, 0);

				break;

			case jendakojednako:

				Code.putFalseJump(Code.eq, 0);

				break;

			}

		}

	}

/////////////////////////// IF ELSE /////////////////////////////////////
	public void visit(UnmatchedIfElse ifelse) {
		if (brPosl.size() > 0) {

			int pom = brPosl.get(brPosl.size() - 1);

			int pom2 = brZaElse.get(brZaElse.size() - 1);

			if (tacniUslovi.size() > 0) {

				Code.fixup(tacniUslovi.get(tacniUslovi.size() - 1));
				tacniUslovi.remove(tacniUslovi.size() - 1);
			}
			brPosl.remove(brPosl.size() - 1);
			for (int i = 0; i < pom; i++) {
				poslednjeAdr.remove(poslednjeAdr.size() - 1);
			}

			brZaElse.remove(brZaElse.size() - 1);
			for (int i = 0; i < pom2; i++) {
				PoslednjalistaZaElse.remove(PoslednjalistaZaElse.size() - 1);
			}

		}
	}

	public void visit(IfNaredba krajifelse) {

		if (brPosl.size() > 0 && brZaElse.size() > 0) {

			int pom = brPosl.get(brPosl.size() - 1);

			int pom2 = brZaElse.get(brZaElse.size() - 1);
			if (tacniUslovi.size() > 0) {
				Code.fixup(tacniUslovi.get(tacniUslovi.size() - 1));
				tacniUslovi.remove(tacniUslovi.size() - 1);
//				for (int i = 0; i < listaZaElse.size(); i++) {
//					Code.fixup(listaZaElse.get(i));
//					System.out.println(" " + listaZaElse.get(i) + " <- Ako je netacan skace na else");
//				}
//				for (int i = 0; i < poslednjaLista.size(); i++) {
//					Code.fixup(poslednjaLista.get(i));
//					System.out.println(" " + poslednjaLista.get(i) + " <- Ako je netacan skace na else");
//				}

			}
			brPosl.remove(brPosl.size() - 1);
			for (int i = 0; i < pom; i++) {
				poslednjeAdr.remove(poslednjeAdr.size() - 1);
			}

			brZaElse.remove(brZaElse.size() - 1);
			for (int i = 0; i < pom2; i++) {
				PoslednjalistaZaElse.remove(PoslednjalistaZaElse.size() - 1);
			}

		}
	}

	public void visit(ElseNaredba elseNaredba) {
		if (brPosl.size() > 0) {

			int pom = brPosl.get(brPosl.size() - 1);
			poslednjaLista.clear();
			for (int i = pom; i > 0; i--) {
				poslednjaLista.add(poslednjeAdr.get(poslednjeAdr.size() - i));
			}
			tacanIf = Code.pc + 1;
			tacniUslovi.add(tacanIf);
			Code.putJump(0);
			for (int i = 0; i < poslednjaLista.size(); i++) {
				Code.fixup(poslednjaLista.get(i));

			}

		}
	}

	public void visit(UnmatchedIf elseNaredba) {
//		Code.fixup(tacniUslovi.get(tacniUslovi.size() - 1));
//		tacniUslovi.remove(tacniUslovi.size() - 1);
		int pom = brPosl.get(brPosl.size() - 1);
		poslednjaLista.clear();
		for (int i = pom; i > 0; i--) {
			poslednjaLista.add(poslednjeAdr.get(poslednjeAdr.size() - i));
		}

		for (int i = 0; i < poslednjaLista.size(); i++) {
			Code.fixup(poslednjaLista.get(i));

		}

		brPosl.remove(brPosl.size() - 1);
		for (int i = 0; i < pom; i++) {
			poslednjeAdr.remove(poslednjeAdr.size() - 1);
		}
		int pom2 = brZaElse.get(brZaElse.size() - 1);

		listaZaElse.clear();
		for (int i = 0; i < pom2; i++) {
			PoslednjalistaZaElse.remove(PoslednjalistaZaElse.size() - 1);
		}
		brZaElse.remove(brZaElse.size() - 1);

	}

	public void visit(PrviCond cond) {
		// System.out.println(" ************************************************** ");
	}

	public void visit(Condition con) {
		for (int i = poslednjaLista.size() - 1; i >= 0; i--) {
			poslednjeAdr.add(poslednjaLista.get(poslednjaLista.size() - 1 - i));

		}
		brPosl.add(poslednjaLista.size());

		for (int i = listaZaElse.size() - 1; i >= 0; i--) {

			PoslednjalistaZaElse.add(listaZaElse.get(listaZaElse.size() - 1 - i));
		}
		brZaElse.add(listaZaElse.size());

		for (int i = 0; i <= listaZaElse.size() - 2; i++) {
			if ((Code.buf[listaZaElse.get(i) - 1] - 43) == vecejednako) {
				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.lt + 43);
			} else if ((Code.buf[listaZaElse.get(i) - 1] - 43) == vece) {
				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.le + 43);
			} else if ((Code.buf[listaZaElse.get(i) - 1] - 43) == manjejednako) {
				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.gt + 43);
			} else if ((Code.buf[listaZaElse.get(i) - 1] - 43) == manje) {
				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.ge + 43);
			} else if ((Code.buf[listaZaElse.get(i) - 1] - 43) == Code.ne) {
				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.eq + 43);
			} else if ((Code.buf[listaZaElse.get(i) - 1] - 43) == Code.eq) {

				Code.buf[listaZaElse.get(i) - 1] = (byte) (Code.ne + 43);
			}
			if (con.getParent().getClass() == DoWhilePetlja.class) {

				Code.put2(listaZaElse.get(i), (pocetnoDo.get(pocetnoDo.size() - 1) - listaZaElse.get(i)));

			} else {
				Code.fixup(listaZaElse.get(i));
			}

		}
		listaZaElse.clear();
	}

////////////////////DO WHILE/////////////////////////////////
	public void visit(DetektovaoDo prvoDo) {
		pocetnoDo.add(Code.pc + 1);
		brBreak.add(0);
		brCont.add(0);
		doWhileNivo++;

	}

	public void visit(DoWhilePetlja dowhile) {
		int pom = brPosl.get(brPosl.size() - 1);
		poslednjaLista.clear();
		for (int i = pom; i > 0; i--) {
			poslednjaLista.add(poslednjeAdr.get(poslednjeAdr.size() - i));
		}

		for (int i = 0; i < poslednjaLista.size() - 1; i++) {
			Code.fixup(poslednjaLista.get(i));
			// System.out.println(" " + poslednjaLista.get(i) + " <- Ako je netacan uslov za
			// do while");
		}
		if (poslednjaLista.size() - 1 >= 0) {

			Code.buf[poslednjaLista.get(poslednjaLista.size() - 1)
					- 1] = (byte) (inverse[Code.buf[poslednjaLista.get(poslednjaLista.size() - 1) - 1] - 43] + 43);
			// put2(patchAdr, (pc-patchAdr + 1)); a prosledjuje se patchAdr
			if (pocetnoDo.size() > 0) {
//				System.out.println(" Popravlje se adresa " + poslednjaLista.get(poslednjaLista.size() - 1)
//						+ " A skok je na pomeraj "
//						+ (pocetnoDo.get(pocetnoDo.size() - 1) - poslednjaLista.get(poslednjaLista.size() - 1)));
				Code.put2(poslednjaLista.get(poslednjaLista.size() - 1),
						(pocetnoDo.get(pocetnoDo.size() - 1) - poslednjaLista.get(poslednjaLista.size() - 1)));
				pocetnoDo.remove(pocetnoDo.size() - 1);
				
			} else
				System.out.println(" Greskaa!!!! ");
		} else {
//			System.out.println("Ovde nikad ne bi trebalo da udje!");
			if (pocetnoDo.size() > 0) {
				pocetnoDo.remove(pocetnoDo.size() - 1);
			}
		}

		if (brBreak.size() > 0) {
			for (int i = 0; i < brBreak.get(brBreak.size() - 1); i++) {
//				System.out.println(" Popravka breakA " + Code.pc + " " + pocetnoDo.size());
				Code.fixup(breakAdrese.get(breakAdrese.size() - 1));
				breakAdrese.remove(breakAdrese.size() - 1);
			}
			brBreak.remove(brBreak.size() - 1);
		}

		doWhileNivo--;
		poslednjaLista = new ArrayList<>();
		listaZaElse = new ArrayList<>();
		brPosl.remove(brPosl.size() - 1); 
		for (int i = 0; i < pom; i++) {
			poslednjeAdr.remove(poslednjeAdr.size() - 1);
		}
		int pom2 = brZaElse.get(brZaElse.size() - 1);
		brZaElse.remove(brZaElse.size() - 1);
		listaZaElse.clear();
		for (int i = 0; i < pom2; i++) {
			PoslednjalistaZaElse.remove(PoslednjalistaZaElse.size() - 1);
		}
	}

	public void visit(BreakNaredba breakNaredba) {
		breakAdrese.add(Code.pc + 1);
		int pom = brBreak.get(brBreak.size() - 1);
		brBreak.set(brBreak.size() - 1, pom + 1);

//		System.out.println("Ovde je break " + (Code.pc + 1) + " nivo " + doWhileNivo);
		Code.putJump(0);
	}

	public void visit(ContinueNaredba conNar) {
		contAdrese.add(Code.pc + 1);
		int pom = brCont.get(brCont.size() - 1);
		brCont.set(brCont.size() - 1, pom + 1);

//		System.out.println("Ovde je continue " + (Code.pc + 1) + " nivo " + brCont.size());
		Code.putJump(0);
	}

	public void visit(PocetnoWhile pocWhile) {
		if (brCont.size() > 0) {
			for (int i = 0; i < brCont.get(brCont.size() - 1); i++) {
//				System.out.println(" Popravka breakA " + Code.pc + " " + pocetnoDo.size());
				Code.fixup(contAdrese.get(contAdrese.size() - 1));
				contAdrese.remove(contAdrese.size() - 1);
			}
			brCont.remove(brCont.size() - 1);
		}
	}
	///////////////////////////// MOD/////////////////////////////
	public void visit(Program p)
	{
//		System.out.println( poslednje .size() );
//		System.out.println( relop .size() );
//		System.out.println( listAdresa .size() );
//		System.out.println( listaZaElse .size() );
//		System.out.println( poslednjaLista .size() );
//		System.out.println( tacniUslovi .size() );
//		System.out.println( pocetnoDo .size() );
////		System.out.println( pocetnoFor .size() );
////		System.out.println( poslednjeFor .size() );
////		System.out.println( tacnoForFor .size() );
////		System.out.println( izvrsiOp .size() );
////		System.out.println( poslednjaOperacija .size() );
//
//		System.out.println( breakAdrese .size() );
//		System.out.println( brBreak .size() );
//		System.out.println( contAdrese.size() );
//		System.out.println( brCont.size() );
//		System.out.println( poslednjeAdr.size() );
//		System.out.println( brPosl.size() );
//		System.out.println( PoslednjalistaZaElse.size() );
//		System.out.println( brZaElse.size() );
	}

}
