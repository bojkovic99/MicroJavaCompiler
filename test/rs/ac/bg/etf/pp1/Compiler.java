package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(Compiler.class);
		
		Reader br = null;
		try {
			File sourceCode = new File(""+args[0]);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	       
	         
	        Program prog = (Program)(s.value); 
	        Tabela.init();
//        	final Struct boolType = new Struct(Struct.Bool);
//   		Tabela.currentScope().addToLocals(new Obj(Obj.Type,"bool",boolType));
//	        Obj objekat = new Obj(Obj.Type,"bool",boolType);
//	        log.info("========================================" + objekat.getKind()+" "+objekat.getName()+" "+objekat.getType());
//	        Tab.insert(objekat.getKind(),objekat.getName(),objekat.getType());
	      
	        log.info("========================================");
			// ispis sintaksnog stabla
		    log.info(prog.toString("")); 
			log.info("==================================="); 

			// ispis prepoznatih programskih konstrukcija
			SemanticAnalyzer sa = new SemanticAnalyzer();
			prog.traverseBottomUp(sa);
			
	//		log.info("===============================================");
			
			Tabela.dump();
			
			if( sa.passed() && !p.error_det){
				File objFile = new File(""+args[1]);
				if(objFile.exists()) objFile.delete();
				
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.dataSize = sa.nVars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));
				if(codeGenerator.postojiMain())
				{
					log.info("Parsiranje uspesno zavrseno!");
				}
				else
				{
					log.info("NE POSTOJI MAIN FUNKCIJA!");
				}
				
				
			}else{
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}
				
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
