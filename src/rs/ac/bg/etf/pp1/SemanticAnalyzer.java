package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.*;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.structure.HashTableDataStructure;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class SemanticAnalyzer extends VisitorAdaptor {
	List<Obj> objekti = new ArrayList<>();
	Obj currentMethod = null;
	boolean returnFound = false;

	int nVars;
	int varCount;
	int currentCharConst;
	int currentBoolConst;
	int currentIntConst;
	int prvaChar;
	int prvaBool;
	int prvaInt;
	boolean errorDetected = false;
	boolean novaDodela = false;
	int brojForArgs = 0;
	ArrayList<Obj> funkcije = new ArrayList<>();
	ArrayList<Struct> parametri = new ArrayList<>();
	int brojac = 0;
	boolean nasaoReturn = false;
	int nivoDo = 0;

	HashTableDataStructure sim = new HashTableDataStructure();
	int currentScopeLevel = 0;

	Logger log = Logger.getLogger(getClass());

	public boolean passed() {
		return !errorDetected;
	}

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(ImePrograma imePrograma) {
		imePrograma.obj = Tab.insert(Obj.Prog, imePrograma.getImePrograma(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getImePrograma().obj);
		Tab.closeScope();
	}

	public void visit(KonstantaNum konst) {
		konst.struct = Tab.intType;
		currentIntConst = konst.getNumerickaKonstanta();
	}

	public void visit(KonstantaCHAR konst) {
		konst.struct = Tab.charType;
		String str = konst.getSlovnaKonstanta();
		char c = str.charAt(1);
		currentCharConst = c;
	}

	public void visit(KonstantaBOOL konst) {
		konst.struct = Tabela.boolType;
		String cd = konst.getTacnoNetacno();
		if (cd.equals("true"))
			currentBoolConst = 1;
		else
			currentBoolConst = 0;
	}

	public void visit(MozdaKonstantica mozKon) {
		int vrednost = 0;
		mozKon.struct = mozKon.getKonstante().struct;

		if (mozKon.struct.equals(Tab.charType)) {
			vrednost = currentCharConst;
			sim.insertKey(
					new Obj(Obj.Con, mozKon.getImeKonstNiz(), mozKon.struct, currentCharConst, currentScopeLevel));

		} else if (mozKon.struct.equals(Tab.intType)) {
			vrednost = currentIntConst;
			sim.insertKey(new Obj(Obj.Con, mozKon.getImeKonstNiz(), mozKon.struct, currentIntConst, currentScopeLevel));

		} else if (mozKon.struct.equals(Tabela.boolType)) {
			vrednost = currentBoolConst;
			sim.insertKey(
					new Obj(Obj.Con, mozKon.getImeKonstNiz(), mozKon.struct, currentBoolConst, currentScopeLevel));
		}
		mozKon.struct.setMembers(sim);


	}

	public void visit(NoKonstanta mozKon) {
		mozKon.struct = Tab.noType;
	}

	public void visit(PrvaConst mozKon) {
		mozKon.struct = mozKon.getKonstante().struct;
		if (mozKon.struct.equals(Tab.charType)) {

			prvaChar = currentCharConst;
		} else if (mozKon.struct.equals(Tab.intType)) {
			prvaInt = currentIntConst;
//			report_info("Deklarisana je konstanta " + mozKon.getImeKonstante() + " sa vrednoscu " + currentIntConst,
//					mozKon);
		} else if (mozKon.struct.equals(Tabela.boolType)) {
			prvaBool = currentBoolConst;
		}

	}

	public void visit(ConstDekl cd) {

		if (cd.getPrvaConst().struct != Tab.noType) {
			if (cd.getPrvaConst().struct.equals(cd.getType().struct)) {
				Obj obj = Tab.find(cd.getPrvaConst().getImeKonstante());
				if (obj != Tab.noObj) {
					report_error("Konstanta sa imenom " + cd.getPrvaConst().getImeKonstante()
							+ " je vec deklarisana! Pokusaj ponovnog deklarisanja je ", cd);
				} else {
					Obj konstanta = Tab.insert(Obj.Con, cd.getPrvaConst().getImeKonstante(), cd.getPrvaConst().struct);
					if (konstanta.getType() == Tab.charType) {
						konstanta.setAdr(prvaChar);
					} else if (konstanta.getType() == Tab.intType) {
						konstanta.setAdr(prvaInt);
					} else {
						konstanta.setAdr(prvaBool);
					}
					report_info("Deklarisana je konstanta " + cd.getPrvaConst().getImeKonstante(), cd);

				}

			} else {
				report_error("Konstanta " + cd.getPrvaConst().getImeKonstante()
						+ " se ne moze deklarisati jer nije odgovarajuceg tipa ", null);
			}

		}

		if (cd.getMozdaKonstante().struct != Tab.noType) {
			MozdaKonstantica mozKon = (MozdaKonstantica) cd.getMozdaKonstante();
			SymbolDataStructure sim = mozKon.struct.getMembersTable();
			for (Obj s : sim.symbols()) {
				if (s.getType() != Tab.noType) {
					if (s.getType().equals(cd.getType().struct)) {
						Obj obj = Tab.find(s.getName());
						if (obj != Tab.noObj) {
							report_error("Konstanta naziva " + mozKon.getImeKonstNiz() + " je vec deklarisana! ", null);
						} else {
							Obj kon = Tab.insert(Obj.Con, s.getName(), mozKon.struct);
							kon.setAdr(s.getAdr());
							report_info("Deklarisana je konstanta " + s.getName(), cd);
						}

					} else {
						report_error("Konstanta " + mozKon.getImeKonstNiz()
								+ " se ne moze deklarisati jer nije odgovarajuceg tipa ", cd);

					}

				}

			}

		}
		sim = new HashTableDataStructure();

	}

	public void visit(JesteNiz niz) {
		niz.struct = new Struct(Struct.Array);
	}

	public void visit(NijeNiz niz) {
		niz.struct = Tab.noType;
	}

	public void visit(ImaJosPars par) {
//		report_info(" Nova Promenljiva u ImaJosParametara  " + par.getType().struct.getKind(), par);
//		
//		par.obj = new Obj(Obj.Var, par.getKoZnaKo(), par.getType().struct);
//		objekti.add(par.obj);

	}

	public void visit(ParametarFje param) {
		brojForArgs++;
		if (param.getMozdaNiz().struct.equals(Tab.noType)) {

			Tab.insert(Obj.Var, param.getKoZnaKo(), param.getType().struct);
			report_info("Deklarisana promenljiva " + param.getKoZnaKo() + " tipa " + param.getType().getTypeName(),
					param);
		} else {
			param.getMozdaNiz().struct.setElementType(param.getType().struct);
			Tab.insert(Obj.Var, param.getKoZnaKo(), param.getMozdaNiz().struct);
			report_info("Deklarisan je niz " + param.getKoZnaKo() + " tipa " + param.getType().getTypeName(), param);

		}

	}

	public void visit(ImaJosParametara par) {
		report_info(" Nova Promenljiva u ImaJosParametara  ", par);
		par.obj = new Obj(Obj.Var, par.getNovaProm().obj.getName(), par.getNovaProm().obj.getType());
		objekti.add(par.obj);

	}

	public void visit(NemaViseParametara par) {
		par.obj = new Obj(Obj.Fld, " ", Tab.noType);
	}

	public void visit(NovaPromenljivaPostoji novap) {
		if (novap.getMozdaNiz().struct.equals(Tab.noType)) {
			novap.obj = new Obj(Obj.Var, novap.getImeNovePromenljive(), Tab.noType);
		} else {
			novap.obj = new Obj(Obj.Var, novap.getImeNovePromenljive(), new Struct(Struct.Array));
		}

		report_info(" Nova Promenljiva  " + novap.getImeNovePromenljive(), novap);
	}

	public void visit(PrviArgumentic param) {
		brojForArgs++;
		Obj obj = Tab.find(param.getFormalniParametar());
		if (param.getMozdaNiz().struct.equals(Tab.noType)) {

			Tab.insert(Obj.Var, param.getFormalniParametar(), param.getType().struct);
			report_info("Deklarisana promenljiva " + param.getFormalniParametar() + " tipa "
					+ param.getType().getTypeName(), param);
		} else {
			param.getMozdaNiz().struct.setElementType(param.getType().struct);
			Tab.insert(Obj.Var, param.getFormalniParametar(), param.getMozdaNiz().struct);
			report_info("Deklarisan je niz " + param.getFormalniParametar() + " tipa " + param.getType().getTypeName(),
					param);

		}

//		if (objekti.size() > 0) {
//			for (int i = 0; i < objekti.size(); i++) {
//				if (objekti.get(i).getType().equals(Tab.noType)) {
//					Obj nasao = Tab.find(objekti.get(i).getName());
//					if (nasao != Tab.noObj && nasao.getAdr() == currentScopeLevel) {
//						report_error("Promenljiva imena " + objekti.get(i).getName() + " je vec deklarisana", null);
//					} else {
//						Tab.insert(Obj.Var, objekti.get(i).getName(), param.getType().struct);
//						report_info(" Deklarisana je nova promenljiva imena " + objekti.get(i).getName(), param);
//					}
//
//				} else {
//					Obj nasao = Tab.find(objekti.get(i).getName());
//					if (nasao != Tab.noObj && nasao.getAdr() == currentScopeLevel) {
//						report_error("Niz imena " + objekti.get(i).getName() + " je vec deklarisana", null);
//					} else {
//						Struct niz = new Struct(Struct.Array);
//						niz.setElementType(param.getType().struct);
//						Tab.insert(Obj.Var, objekti.get(i).getName(), niz);
//						report_info(" Deklarisan je novi niz promenljivih imena " + objekti.get(i).getName(), param);
//					}
//
//				}
//			}
//
//		}

		objekti = new ArrayList<>();

	}

	public void visit(VarDekla varDekla) {
		varCount++;
		Obj obj = Tab.find(varDekla.getImePromenljive());
		if (obj == Tab.noObj) {
			if (varDekla.getMozdaNiz().struct.equals(Tab.noType)) {

				Tab.insert(Obj.Var, varDekla.getImePromenljive(), varDekla.getType().struct);
				report_info("Deklarisana promenljiva " + varDekla.getImePromenljive() + " tipa "
						+ varDekla.getType().getTypeName(), varDekla);
			} else {
				varDekla.getMozdaNiz().struct.setElementType(varDekla.getType().struct);
				Tab.insert(Obj.Var, varDekla.getImePromenljive(), varDekla.getMozdaNiz().struct);
				report_info("Deklarisan je niz " + varDekla.getImePromenljive() + " tipa "
						+ varDekla.getType().getTypeName(), varDekla);

			}

		} else {
			report_error("Promenljiva naziva " + varDekla.getImePromenljive()
					+ " je vec deklarisana unutar datog opsega! Greska ", varDekla);
		}

		if (objekti.size() > 0) {
			for (int i = 0; i < objekti.size(); i++) {
				if (objekti.get(i).getType().equals(Tab.noType)) {
					Obj nasao = Tab.find(objekti.get(i).getName());
					if (nasao != Tab.noObj && nasao.getAdr() == currentScopeLevel) {
						report_error("Promenljiva imena " + objekti.get(i).getName() + " je vec deklarisana", null);
					} else {
						Tab.insert(Obj.Var, objekti.get(i).getName(), varDekla.getType().struct);
						report_info(" Deklarisana je nova promenljiva imena " + objekti.get(i).getName(), varDekla);
					}

				} else {
					Obj nasao = Tab.find(objekti.get(i).getName());
					if (nasao != Tab.noObj && nasao.getAdr() == currentScopeLevel) {
						report_error("Niz imena " + objekti.get(i).getName() + " je vec deklarisana", null);
					} else {
						Struct niz = new Struct(Struct.Array);
						niz.setElementType(varDekla.getType().struct);
						Tab.insert(Obj.Var, objekti.get(i).getName(), niz);
						report_info(" Deklarisan je novi niz promenljivih imena " + objekti.get(i).getName(), varDekla);
					}

				}
			}

		}

		objekti = new ArrayList<>();

	}

	public void visit(Type type) {
		Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
			type.struct = Tab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
				type.struct = Tab.noType;
			}
		}
	}

	public void visit(ImaPovratnuVrednost povr) {
		currentMethod = Tab.insert(Obj.Meth, povr.getImeFunkcije(), povr.getType().struct);
		povr.obj = currentMethod;
		currentScopeLevel++;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + povr.getImeFunkcije(), povr);
	}

	public void visit(PovratnaVrVoid povr) {
		currentMethod = Tab.insert(Obj.Meth, povr.getImeFje(), Tab.noType);
		povr.obj = currentMethod;
		currentScopeLevel++;
		Tab.openScope();
		// currentScopeLevel--;
		report_info("Obradjuje se void funkcija " + povr.getImeFje(), povr);
	}

	public void visit(Parametard param) {
		currentMethod.setLevel(brojForArgs);
	}

	public void visit(ZavrsenaFunkcija zavrsena) {

		if (currentMethod.getType() != Tab.noType && nasaoReturn == false) {
			report_error("Ne postoji return naredba za funkciju koja nije tipa void " + currentMethod.getName(),
					zavrsena);

		}
		nasaoReturn = false;
		brojForArgs = 0;
		report_info("Za funkciju " + currentMethod.getName() + " deklarisana su " + currentMethod.getLevel()
				+ " parametra ", zavrsena);
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentScopeLevel--;
		brojac = 0;

		currentMethod = null;
	}
	/*
	 * public void visit(Designator designator) { Obj obj =
	 * Tab.find(designator.getDes()); // report_info("Tip je "+
	 * obj.getType().getElemType().getKind(), designator); if (obj == Tab.noObj) {
	 * report_error("Greska na liniji " + designator.getLine() + " : ime " +
	 * designator.getDes() + " nije deklarisano! ", null); } else {
	 * 
	 * 
	 * if (designator.getListaDes().obj.getType().equals(Tab.noType)) {
	 * designator.obj = obj; } else { // tip objekta je niz, element niza if
	 * (obj.getType().getKind() == 3) { // designator.obj =
	 * designator.getListaDes().obj; designator.obj = new Obj(obj.getKind(),
	 * obj.getName(), obj.getType().getElemType(),obj.getAdr(),obj.getLevel() );
	 * report_info("Tip je "+ obj.getType().getElemType().getKind(), designator);
	 * 
	 * } else { report_error("Greska na liniji " + designator.getLine() + " : ime "
	 * + designator.getDes() + " nije niz! ", null); } }
	 * 
	 * } // designator.obj = obj;
	 * 
	 * }
	 */

	String ime;

	public void visit(ElementNiza elem) {
		elem.obj = Tab.find(elem.getDes());
		ime = elem.getDes();
	}

	public void visit(DesignatorNiz designator) {
		Obj obj = designator.getImeNiza().obj;

		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + ime + " nije deklarisano! ", null);
		} else if (obj.getType().getKind() == 3) {
			designator.obj = new Obj(obj.getKind(), obj.getName(), obj.getType().getElemType(), obj.getAdr(),
					obj.getLevel());

		} else {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + ime + " nije niz! ", null);

		}

	}

	public void visit(ObicanBroj designator) {
		Obj obj = Tab.find(designator.getDes());

		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getDes()
					+ " nije deklarisano! ", null);
		} else {

			if (obj.getKind() == Obj.Meth) {
				funkcije.add(obj);
			}

			designator.obj = obj;
		}

	}

	public void visit(ImaListeDes des) {
		Struct str = new Struct(Struct.Array);
		str.setElementType(Tab.intType);
// || (des.getExpr().struct.getKind() == 3 && des.getExpr().struct.getElemType().equals(Tab.intType))
		if (des.getExpr().struct == Tab.intType) {

		} else {
			report_error("Unutar uglastih zagrada se ne nalazi tip int! , greska ", des.getExpr());
		}
		des.obj = new Obj(Obj.Var, " ", str);
	}

	public void visit(NemaListeDes des) {
		des.obj = new Obj(Obj.Var, " ", Tab.noType);
	}

	public void visit(NumerickaKonstanta num) {
		num.struct = Tab.intType;
	}

	public void visit(KarakterKonst num) {
		num.struct = Tab.charType;
	}

	public void visit(BoolKonstanta num) {
		num.struct = Tabela.boolType;
	}

	public void visit(FactorDesignator fdes) {
		if (fdes.getDesignator().obj == null) {
			report_error("Greska! Ne postoji promenljiva!", fdes);
		} else {
			fdes.struct = fdes.getDesignator().obj.getType();
		}

	}

	public void visit(NewSimbol novi) {
		novaDodela = true;
		novi.struct = novi.getType().struct;
	}

	public void visit(NovSimbol novi) {
		novi.struct = novi.getNoviSimbol().struct;
	}

	public void visit(PostojeNoviIzarzi izrazi) {

		if (!izrazi.getExpr().struct.equals(Tab.intType)) {
			report_error("Greska na liniji " + izrazi.getLine() + " : Tip unutar uglastih mora biti int! Prilikom new ",
					null);
		}
	}

	public void visit(NoviIzraz novi) {
		novi.struct = novi.getExpr().struct;
	}

	public void visit(Term term) {
		if (term.getListaMulFact().obj.equals(Tab.noObj)) {
			term.struct = term.getFactor().struct;
		} else {
			if (term.getFactor().struct.getKind() == 3 && term.getFactor().struct.getElemType().equals(Tab.intType)) {
				term.struct = term.getFactor().struct;
			} else if (term.getFactor().struct.equals(Tab.intType)) {
				term.struct = term.getFactor().struct;
			} else if (term.getFactor().struct.getKind() == 3
					&& !term.getFactor().struct.getElemType().equals(Tab.intType)) {
				report_error("Greska na liniji " + term.getLine()
						+ " : factor za operazije mnozenja i deljenja mora biti tipa int !!!!"
						+ term.getFactor().struct.getKind(), null);
			}

		}

	}

	public void visit(PostojiListaMulFact mul) {
		mul.obj = new Obj(Obj.Var, "", Tab.intType);
	}

	public void visit(NemaListaMulFact mul) {
		mul.obj = Tab.noObj;
	}

	public void visit(MulFact term) {
		/*
		 * ZA NIZ || (term.getFactor().struct.getKind() == 3 &&
		 * term.getFactor().struct.getElemType().equals(Tab.intType))
		 * 
		 * 
		 */
		if (term.getFactor().struct.equals(Tab.intType)) {

		} else {
			report_error("Greska na liniji " + term.getLine() + " : Tip kojim se pokusava mnozenje nije int ", null);
		}
	}

	public void visit(PostojiMinusUExpr minus) {
		minus.struct = minus.getTerm().struct;
	}

	public void visit(PostojiMinus minus) {
		minus.obj = new Obj(Obj.Var, " ", Tab.intType);
	}

	public void visit(NemaMinusa minus) {
		minus.obj = Tab.noObj;
	}

	public void visit(IzrazExprassa izraz) {
		izraz.struct = izraz.getTerm().struct;

		if (izraz.struct != null) {

			if (izraz.getMozdaMinus().obj.equals(Tab.noObj)) {
				if (izraz.getListaOperacija().obj.equals(Tab.noObj)) {
					izraz.struct = izraz.getTerm().struct;
				} else {
					if (izraz.getTerm().struct.equals(Tab.intType) || (izraz.getTerm().struct.getKind() == 3
							&& izraz.getTerm().struct.getElemType().equals(Tab.intType))) {
						izraz.struct = izraz.getTerm().struct;
					} else
						report_error("Greska na liniji " + izraz.getLine() + " : Tip mora biti int! ", null);
				}

			} else {
				if (izraz.getTerm().struct.equals(Tab.intType) || (izraz.getTerm().struct.getKind() == 3
						&& izraz.getTerm().struct.getElemType().equals(Tab.intType))) {
					izraz.struct = Tab.intType;
				} else
					report_error("Greska na liniji " + izraz.getLine() + " : Tip nakon minusa mora biti int ", null);
			}
		}
	}

	public void visit(ObicanExpr izraz) {
		izraz.struct = izraz.getExpr1().struct;
		if (izraz.getParent().getClass() == ListaExpresion.class) {
			brojac++;
			parametri.add(izraz.getExpr1().struct);
		}

	}

	public void visit(JednaOperacija izraz) {
		if (!(izraz.getTerm().struct.equals(Tab.intType))) {
			report_error("Greska na liniji " + izraz.getLine()
					+ " : Tip prilikom operacija sabiranja i osuzimanja mora biti int! ", null);
		}
	}

	public void visit(IzrazExpr iz) {

		if (iz.getPrvaOpcijaTer().struct != iz.getDrugaOpcijaTer().struct) {
			report_error("Drugi i treci izraz ternarnog operatora moraju biti istog tipa ", iz);
		} else {
			iz.struct = iz.getPrvaOpcijaTer().struct;

		}

	}

	public void visit(TesrnarniUslov ter) {
		ter.struct = ter.getExpr1().struct;
	}

	public void visit(TernarniPrvaOpcija ter) {
		ter.struct = ter.getExpr1().struct;
	}

	public void visit(TernarniDrugaOpcija ter) {
		ter.struct = ter.getExpr1().struct;
	}

	public void visit(ListaOperacijaPostoji lista) {
		lista.obj = new Obj(Obj.Var, " ", Tab.intType);
	}

	public void visit(NemaListeOperacija lista) {
		lista.obj = Tab.noObj;
	}

	public void visit(DodelaVrednostiDes dodela) {

		if (dodela.getDesignator().obj == Tab.noObj || dodela.getDesignator().obj == null
				|| dodela.getExpr().struct == null) {
			report_error("Ne postoji struct za Expr", dodela);
		}

		else if (dodela.getDesignator().obj.getKind() != 1) {
			report_error("Greska na liniji " + dodela.getLine()
					+ ": Sa leve strane dodele vrednosti se mora naci promenljiva ili element niza! ", null);
		}

		/*
		 * ZA NIZOVE || ( dodela.getDesignator().obj.getType().getKind() == 3 &&
		 * dodela.getDesignator().obj.getType().getElemType()
		 * .assignableTo(dodela.getOpcije().struct) ) || (
		 * dodela.getOpcije().struct.getKind() == 3 &&
		 * dodela.getDesignator().obj.getType()
		 * .assignableTo(dodela.getOpcije().struct.getElemType()) )
		 * 
		 * 
		 */
		else if (!(dodela.getExpr().struct.assignableTo(dodela.getDesignator().obj.getType())
				|| (novaDodela == true && dodela.getDesignator().obj.getType().getKind() == 3
						&& dodela.getExpr().struct == dodela.getDesignator().obj.getType().getElemType()))) {
			report_error(
					"Greska na liniji " + dodela.getLine() + ": Tipovi podataka prilikom dodele nisu kompatibilni! ",
					null);

		}

		else if (novaDodela == true && dodela.getDesignator().obj.getType().getKind() != 3) {

			report_error(
					"Greska na liniji " + dodela.getLine() + ": Niz " + dodela.getDesignator().obj.getName()
							+ " se ne nalazi sa leve strane dodele " + dodela.getDesignator().obj.getType().getKind(),
					null);
		}
		novaDodela = false;

	}

	public void visit(UnarniPlusPlus dodela) {
		if (dodela.getDesignator().obj.getKind() != 1) {
			report_error("Greska na liniji " + dodela.getLine()
					+ ": Sa leve strane dodele vrednosti se mora naci promenljiva! ", null);
		} else if (!dodela.getDesignator().obj.getType().equals(Tab.intType)) {
			// || (dodela.getDesignator().obj.getType().getKind() == 3 &&
			// dodela.getDesignator().obj.getType().getElemType().equals(Tab.intType))

			report_error("Greska na liniji " + dodela.getLine() + ": Pre operatora ++ se mora nalaziti tip int ", null);

		}

	}

	public void visit(UnarniMinusMinus dodela) {
		if (dodela.getDesignator().obj.getKind() != 1) {
			report_error("Greska na liniji " + dodela.getLine()
					+ ": Sa leve strane dodele vrednosti se mora naci promenljiva! ", null);
		} else if (!dodela.getDesignator().obj.getType().equals(Tab.intType)) {
			// || (dodela.getDesignator().obj.getType().getKind() == 3 &&
			// dodela.getDesignator().obj.getType().getElemType().equals(Tab.intType))

			report_error("Greska na liniji " + dodela.getLine() + ": Pre operatora  -- se mora nalaziti tip int ",
					null);

		}

	}

	public void visit(PovecajZaJedan dodela) {
		dodela.struct = Tab.noType;
		dodela.struct.setElementType(Tab.intType);
	}

	public void visit(SmanjiZaJedan dodela) {
		dodela.struct = Tab.noType;
		dodela.struct.setElementType(Tab.intType);
	}

	public void visit(ReadNaredba read) {
		if (read.getDesignator().obj.getKind() != 1) {
			report_error("Read moze da ucita vrednost samo u promenljivu! Greska ", read);
		}
		if (!(read.getDesignator().obj.getType() == Tab.intType || read.getDesignator().obj.getType() == Tab.charType
				|| read.getDesignator().obj.getType() == Tabela.boolType)) {
			report_error("Read moze da bude samo int, char ili bool! Greska ", read);
		}
	}

	public void visit(PrintNaredba print) {
		if (print.getExpr().struct == Tab.intType || print.getExpr().struct == Tabela.boolType
				|| print.getExpr().struct == Tabela.charType) {

		} else {
			report_error("Print moze da ispise samo obicne tipove! Greska ", print);
		}
	}

	public void visit(ReturnNaredba ret) {
		nasaoReturn = true;
		if (currentMethod.getType() != Tab.noType) {
			report_info("Return naredba!", ret);

			if (ret.getMozdaExpr().struct == Tab.noType || ret.getMozdaExpr().struct == null
					|| ret.getMozdaExpr().getClass() == NePostojiExpr.class) {
				report_error("Nije pronadjen tip za metodu koja ima povratnu vrednost ", ret);
			} else {
				if (!ret.getMozdaExpr().struct.assignableTo(currentMethod.getType())) {
					report_error("Tip nakon return naredbe nije kompatibilan povratnoj vrednosti funkcije ", ret);
				}
			}
		} else {
			if (ret.getMozdaExpr().getClass() != NePostojiExpr.class) {
				report_error("Funkcija je tipa void a postoji return naredba ", ret.getMozdaExpr());
			}

		}
	}

	public void visit(PostojiExpr expr) {
		if (expr.getExpr().struct == Tab.noType || expr.getExpr().struct == null) {
			report_error("Greska expr", expr);
		} else {
			expr.struct = expr.getExpr().struct;
		}
	}

	public void visit(CondFactd cond) {
		if (!cond.getExprPomRel().struct.assignableTo(cond.getExpr().struct)) {
			report_error("Tipovi prilikom poredjenja nisu kompatibilni ! !", cond);
		} else {

		}

	}

	public void visit(ExpoPrvi ex) {
		ex.struct = ex.getExpr().struct;
		if (ex.getExpr().struct.getKind() == 3) {
			if (ex.getRelop().getClass() != Isto.class && ex.getRelop().getClass() != Razlicito.class) {
				report_error("Tipovi prilikom poredjenja nisu kompatibilni! Za poredjenje nizova se mogu koristiti samo == ili != Greska ", ex);
			}
		} else {

		}
	}
///////////////////////////////////////// FUNKCIJE ////////////////////////////////////////////
	public void visit(Pozivi poz) {
		poz.struct = poz.getPomocni().struct;
		brojac = 0;
		// System.out.println("RESTART BROJACA");

	}

	public void visit(PomocniPostoji pom) {
		if (pom.getDesignator().obj == null) {
			report_error("Greska! Ne postoji promenljiva!   ", pom);
		} else {
			pom.struct = pom.getDesignator().obj.getType();
			if (pom.getDesignator().obj.getKind() != Obj.Meth) {
				report_error(" Greska! " + pom.getDesignator().obj.getName() + " nije funkcija!", pom);
			} else {

			}

		}
	}

	public void visit(PostojiActPars act) {

		if (funkcije.size() > 0) {
			Obj fja = funkcije.remove(funkcije.size() - 1);
		
			if (fja.getName().equals("ord")) {
				if (parametri.get(parametri.size() - 1).getKind() != 2) {
					report_error("Parametar funkcije odr mora biti tipa char ", null);
					return;
				}

			}
			if (fja.getName().equals("chr")) {
				if (parametri.get(parametri.size() - 1).getKind() != 1) {
					report_error("Parametar funkcije chr mora biti tipa int ", null);
					return;
				}

			}
			if (fja.getName().equals("len")) {
				if (parametri.get(parametri.size() - 1).getKind() != 3) {
					report_error("Parametar funkcije len mora biti tipa array ", null);
					return;
				}

			}
			if (fja.getLevel() == 0 && act.getActParsLista().getClass() != SamoJedanClan.class) {
				report_error("Funkcija " + fja.getName() + " nema parametre! Greska ", act);
			} else if (fja.getLevel() != 0 && act.getActParsLista().getClass() == SamoJedanClan.class) {
				report_error("Funkciji " + fja.getName() + " nisu prosledjeni parametri! Greska ", act);
			} else {
				if (brojac > 0 && parametri.size() > 0) {
					Iterator<Obj> parami = fja.getLocalSymbols().iterator();
					for (int j = 0; j <= parametri.size() - 1; j++) {
						// System.out.println(parametri.get(j).getKind());
					}

					int i = brojac;
					int broj = fja.getLevel();

					if (broj == brojac) {
						i = brojac;
						while (i != 0 && parami.hasNext()) {

							Obj param = parami.next();
							// System.out.println(parametri.get(parametri.size() - i ).getKind()+" ==
							// "+param.getType().getKind());

							if (param.getType().getKind() == parametri.get(parametri.size() - i).getKind()) {

							} else {
								report_error("Parametar " + (brojac - i + 1) + "  koji se prosledjuje funkciji "
										+ fja.getName() + " je pogresan ", act);
							}

							i--;
						}

					} else {
						report_error("Pogresan broj parametara funkcije! " + brojac + " " + broj, act);
					}

				}
			}
		} else {
			//report_error("Greska! Linija " + act.getLine() + ": data funkcija nema parametre! ", null);
		} 
		brojac = 0;

	}

	public void visit(ActParsListaPostoji param) {

	}

	public void visit(ActParsPrvi param) {
		
		brojac++;
		parametri.add(param.getExpr().struct);
		

	}

	public void visit(FunkcijaSaArgumentima fun) {
		if (fun.getDesignator().obj.getKind() != 3) {
			report_error("Zadato ime nije funkcija! ", fun);
		}

		Obj fja = fun.getDesignator().obj;
		if (fja.getName().equals("ord")) {
			if (parametri.get(parametri.size() - 1).getKind() != 2) {
				report_error("Parametar funkcije odr mora biti tipa char ", null);
				return;
			}

		}
		if (fja.getName().equals("chr")) {
			if (parametri.get(parametri.size() - 1).getKind() != 1) {
				report_error("Parametar funkcije chr mora biti tipa int ", null);
				return;
			}

		}
		if (fja.getName().equals("len")) {
			if (parametri.get(parametri.size() - 1).getKind() != 3) {
				report_error("Parametar funkcije len mora biti tipa array ", null);
				return;
			}

		}

		if (brojac > 0 && parametri.size() > 0) {
			Iterator<Obj> parami = fja.getLocalSymbols().iterator();
			int i = brojac;
			int broj = fja.getLevel();
			if (broj == brojac) {
				i = brojac;
				while (i != 0 && parami.hasNext()) {

					Obj param = parami.next();
					// System.out.println(parametri.get(parametri.size() - i ).getKind()+" ==
					// "+param.getType().getKind());

					if (param.getType().getKind() == parametri.get(parametri.size() - i).getKind()) {

					} else {
						report_error("Parametar " + (brojac - i + 1) + "  koji se prosledjuje funkciji " + fja.getName()
								+ " je pogresan ", null);
					}

					i--;
				}

			} else {
				report_error("Pogresan broj parametara funkcije! " + brojac + " " + broj, null);
			}

		}

		brojac = 0;
		// System.out.println("FunkcijaSaArgumentima");

	}

	public void visit(FunkcijaBezArgumenata fun) {
		Obj fja = fun.getDesignator().obj;
		if (fun.getDesignator().obj.getKind() != 3) {
			report_error("Zadato ime nije funkcija!! ", fun);
		}

		if (fja.getLevel() > 0) {
			report_error("Funkciji treba da se proslede argumenti! ", fun);
		}
		else if(brojac > 0)
		{
			report_error("Funkcija ne prima argumente! ", fun);
		}

		brojac = 0;
		// System.out.println("FunkcijaSaArgumentima");

	}
//////////////////////// DO WHILE ///////////////////////
	public void visit(DetektovaoDo detDo) {
		nivoDo++;
		
	}

	public void visit(DoWhilePetlja dowhile) {
		nivoDo--;
		
	}

	public void visit(BreakNaredba breakNar) {
		if (nivoDo == 0) {
			report_error("Break se moze nalaziti samo unutar DO WHILE petlje " + nivoDo, null);
		}
//		else
//			nivoDo--;
	}

	public void visit(ContinueNaredba con) {
		if (nivoDo == 0) {
			report_error("Continue se moze nalaziti samo unutar DO WHILE petlje", null);
		}
//		else
//			nivoDo--;
	}

}
