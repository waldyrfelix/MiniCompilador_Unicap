package br.unicap.comp.parser;

import br.unicap.comp.ClasseToken;
import br.unicap.comp.CodigosTokens;
import br.unicap.comp.DicionarioPalavrasReservadas;
import br.unicap.comp.Token;
import br.unicap.comp.codegen.Gen;
import br.unicap.comp.scanner.Scan;

public class Parse {

	private Scan scan;
	private Token token;
	int escopo;

	public Parse() {
		this.escopo = 0;
	}

	public void executar(String arquivo) throws Exception {
		scan = new Scan(arquivo);
		proxToken();
		lerPrograma();
	}

	// valida o token e obtem o pr�ximo
	private boolean validarToken(int codigoToken) throws Exception {
		if (token != null && token.getCodigoToken() == codigoToken) {
			proxToken();
			return true;
		} else
			return false;
	}

	// obtem o proximo
	private void proxToken() throws Exception {
		token = scan.proximoToken();
	}

	// valida o token e obtem o proximo
	private boolean validarToken(ClasseToken identificador) throws Exception {
		if (token != null && token.getClasseDoToken() == identificador) {
			proxToken();
			return true;
		} else
			return false;
	}

	private void lerPrograma() throws Exception {
		if (validarToken(CodigosTokens.PALAVRA_INT)) {
			if (validarToken(CodigosTokens.PALAVRA_MAIN)) {
				if (validarToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES)) {
					if (validarToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES)) {
						lerBloco();
					} else
						erroSintatico("Token esperado ')'.");
				} else
					erroSintatico("Token esperado '('.");
			} else
				erroSintatico("Fun��o main esperada.");
		} else
			erroSintatico("Palavra reservada 'int' esperada.");
	}

	// ler um bloco da linguagem
	private ExpressaoSemantica lerBloco() throws Exception {
		// se for um '{' ent�o � um bloco, caso contrario d� erro de compila��o
		if (!validarToken(CodigosTokens.ESPECIAL_ABRE_CHAVES)) {
			erroSintatico("Token esperado '{'.");
		}

		// incrementa o contador de escopo
		escopo++;

		// se for char, float ou int ent�o � declara��o de variavel
		// first(decl_var) = {char, int, float}
		while (token != null
				&& (token.getCodigoToken() == CodigosTokens.PALAVRA_CHAR
						|| token.getCodigoToken() == CodigosTokens.PALAVRA_FLOAT || token
						.getCodigoToken() == CodigosTokens.PALAVRA_INT)) {
			lerDeclVariavel();
		}

		// ler comando
		// first(comando) = {while, do, {, if}
		while (token != null
				&& (token.getCodigoToken() == CodigosTokens.PALAVRA_WHILE
						|| token.getCodigoToken() == CodigosTokens.PALAVRA_DO
						|| token.getCodigoToken() == CodigosTokens.PALAVRA_IF
						|| token.getCodigoToken() == CodigosTokens.ESPECIAL_ABRE_CHAVES || token
						.getClasseDoToken() == ClasseToken.Identificador)) {
			lerComando();
		}

		// se o token n�o for fecha chave d� erro de compila��o
		if (!validarToken(CodigosTokens.ESPECIAL_FECHA_CHAVES)) {
			erroSintatico("Token esperado '}'.");
		}

		// remove os simbolos do escopo atual
		TabelaDeSimbolos.excluirSimbolos(this.escopo);

		// remove o escopo
		escopo--;

		return null;
	}

	// ler um comando da linguagem
	private ExpressaoSemantica lerComando() throws Exception {

		// l� comando b�sico
		// first(comando_basico) = { <id>, { }
		if (token != null
				&& (token.getClasseDoToken() == ClasseToken.Identificador || token
						.getCodigoToken() == CodigosTokens.ESPECIAL_ABRE_CHAVES)) {
			return lerComandoBasico();
		}

		// l� itera��o
		// first(comando_basico) = { while, do }
		else if (token != null
				&& (token.getCodigoToken() == CodigosTokens.PALAVRA_WHILE || token
						.getCodigoToken() == CodigosTokens.PALAVRA_DO)) {
			return lerIteracao();
		}

		// l� express�o condicional
		// first (if) = { if }
		else if (validarToken(CodigosTokens.PALAVRA_IF)) {
			if (validarToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES)) {

				ExpressaoSemantica cond = lerExprRelacional();
				String l1 = Gen.geraLabel(false);
				Gen.geraIf(cond.getVarTemp(), cond.getOperador(), l1);
				if (validarToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES)) {
					lerComando();

					String l2 = Gen.geraLabel(false);
					Gen.geraGoto(l2);
					Gen.geraLabel(l1);
					if (validarToken(CodigosTokens.PALAVRA_ELSE)) {
						lerComando();
					}
					Gen.geraLabel(l2);
				} else
					erroSintatico("Token esperado ')'.");

			} else
				erroSintatico("Token esperado '('.");
		} else {
			erroSintatico("Esperado um comando if, while ou do, identificador ou token '{'.");
		}
		return null;
	}

	private ExpressaoSemantica lerExprRelacional() throws Exception {

		// l� a express�o aritm�tica � esquerda
		ExpressaoSemantica expr1 = lerExprAritmetica();

		String opera = DicionarioPalavrasReservadas.getPalavra(token
				.getCodigoToken());

		if (!(validarToken(CodigosTokens.OPERADOR_RELAC_DIFERENTE)
				|| validarToken(CodigosTokens.OPERADOR_RELAC_IGUAL)
				|| validarToken(CodigosTokens.OPERADOR_RELAC_MAIOR)
				|| validarToken(CodigosTokens.OPERADOR_RELAC_MAIOR_IGUAL)
				|| validarToken(CodigosTokens.OPERADOR_RELAC_MENOR) || validarToken(CodigosTokens.OPERADOR_RELAC_MENOR_IGUAL))) {
			erroSintatico("Operador relacional esperado.");
		}

		// l� express�o aritim�tica � direita
		ExpressaoSemantica expr2 = lerExprAritmetica();

		// faz convers�o explicita para float
		if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

		} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));

		}
		
		// gera saida
		int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
		String var = expr1.getVarTemp() + opera + expr2.getVarTemp();
		return new ExpressaoSemantica(var, tipo, opera);
	}

	private ExpressaoSemantica lerIteracao() throws Exception {
		if (validarToken(CodigosTokens.PALAVRA_WHILE)) {

			String l1 = Gen.geraLabel(true);

			if (validarToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES)) {

				ExpressaoSemantica expr = lerExprRelacional();

				String l2 = Gen.geraIf(expr.getVarTemp());

				String l3 = Gen.geraLabel(false);
				Gen.geraGoto(l3);

				Gen.geraLabel(l2);

				if (validarToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES)) {
					lerComando();

					Gen.geraGoto(l1);
					Gen.geraLabel(l3);
				} else
					erroSintatico("Token ')' esperado.");

			} else
				erroSintatico("Token '(' esperado.");

		} else if (validarToken(CodigosTokens.PALAVRA_DO)) {

			String l1 = Gen.geraLabel(true);
			lerComando();

			if (validarToken(CodigosTokens.PALAVRA_WHILE)) {

				if (validarToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES)) {

					ExpressaoSemantica expr = lerExprRelacional();
					Gen.geraIf(expr.getVarTemp(), l1);

					if (!validarToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES)) {
						erroSintatico("Token ')' esperado.");
					}

					if (!validarToken(CodigosTokens.ESPECIAL_PONTO_E_VIRGULA)) {
						erroSintatico("Token ';' esperado.");
					}

				} else
					erroSintatico("Token '(' esperado.");
			} else
				erroSintatico("Token 'while' esperado.");
		} else {
			erroSintatico("Comando while ou do esperado.");
		}
		return null;
	}

	private ExpressaoSemantica lerComandoBasico() throws Exception {
		// ler atribui��o
		// first(atribui��o) = {<id>}
		if (token.getClasseDoToken() == ClasseToken.Identificador) {
			return lerAtribuicao();
		}
		// l� bloco
		// first(bloco) = { { }
		else if (token.getCodigoToken() == CodigosTokens.ESPECIAL_ABRE_CHAVES) {
			return lerBloco();
		} else {
			erroSintatico("Identificador ou '{' esperado.");
		}
		return null;
	}

	private ExpressaoSemantica lerAtribuicao() throws Exception {

		Token tk = this.token;

		if (!validarToken(ClasseToken.Identificador)) {
			erroSintatico("Identificador de vari�vel esperado.");
		}

		if (!TabelaDeSimbolos.existeSimbolo(tk.getLexema())) {
			erroSemantico("Vari�vel " + tk.getLexema() + " n�o declarada.");
		}

		if (!validarToken(CodigosTokens.OPERADOR_ARIT_ATRIBUICAO)) {
			erroSintatico("Token '=' esperado.");
		}

		ExpressaoSemantica expr = lerExprAritmetica();
		Simbolo simb = TabelaDeSimbolos.getSimbolo(tk.getLexema(), escopo);
		if (simb.getTipo() != expr.getTipo()) {
			if (simb.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr.getTipo() == CodigosTokens.PALAVRA_INT) {
				expr.setVarTemp(Gen.geraConversao(expr.getVarTemp()));
			} else {
				erroSemantico("Tipos incompat�veis.");
			}
		}

		if (!validarToken(CodigosTokens.ESPECIAL_PONTO_E_VIRGULA)) {
			erroSintatico("Token ';' esperado.");
		}

		expr.setVarTemp(Gen.atrib(simb.getNome(), expr.getVarTemp()));
		return expr;
	}

	private ExpressaoSemantica lerExprAritmetica() throws Exception {
		// l� termo
		ExpressaoSemantica expr1 = lerTermo();
		// l� expr_arit'
		ExpressaoSemantica expr2 = auxExprAritmetica();

		if (expr2 == null)
			return expr1;

		// faz convers�o explicita para float
		if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

		} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));

		}

		int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
		String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador()
				+ expr2.getVarTemp());
		return new ExpressaoSemantica(var, tipo);
	}

	private ExpressaoSemantica auxExprAritmetica() throws Exception {
		if (validarToken(CodigosTokens.OPERADOR_ARIT_ADICAO)) {

			ExpressaoSemantica expr1 = lerTermo();
			ExpressaoSemantica expr2 = auxExprAritmetica();

			if (expr2 == null) {
				expr1.setVarTemp(expr1.getVarTemp());
				expr1.setOperador("+");
				return expr1;
			}

			// faz convers�o explicita para float
			if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

			} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));
			}

			int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
			String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador()
					+ expr2.getVarTemp());
			return new ExpressaoSemantica(var, tipo, "+");

		} else if (validarToken(CodigosTokens.OPERADOR_ARIT_SUBTRACAO)) {
			ExpressaoSemantica expr1 = lerTermo();
			ExpressaoSemantica expr2 = auxExprAritmetica();

			if (expr2 == null) {
				expr1.setVarTemp(expr1.getVarTemp());
				expr1.setOperador("-");
				return expr1;
			}

			// faz convers�o explicita para float
			if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

			} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));
			}

			int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
			String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador()
					+ expr2.getVarTemp());
			return new ExpressaoSemantica(var, tipo, "-");
		}
		return null;
	}

	private ExpressaoSemantica lerTermo() throws Exception {
		ExpressaoSemantica expr1 = lerFator();
		ExpressaoSemantica expr2 = auxTermo();

		if (expr2 == null)
			return expr1;

		// faz convers�o explicita para float
		if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

		} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
				&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

			expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));

		}

		int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
		String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador()+expr2.getVarTemp());
		
		if (expr2.getOperador() == "/" && tipo == CodigosTokens.PALAVRA_INT){
			var = Gen.geraConversao(var);
			tipo = CodigosTokens.PALAVRA_FLOAT;
		}
		
		return new ExpressaoSemantica(var, tipo);
	}

	private ExpressaoSemantica auxTermo() throws Exception {
		if (validarToken(CodigosTokens.OPERADOR_ARIT_DIVISAO)) {

			ExpressaoSemantica expr1 = lerFator();
			ExpressaoSemantica expr2 = auxTermo();

			if (expr2 == null) {
				expr1.setVarTemp(expr1.getVarTemp());
				expr1.setOperador("/");
				return expr1;
			}

			// faz convers�o explicita para float
			if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

			} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));

			}

			int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
			String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador() + expr2.getVarTemp());
			return new ExpressaoSemantica(var, tipo, "/");

		} else if (validarToken(CodigosTokens.OPERADOR_ARIT_MULTIPLICACAO)) {
			ExpressaoSemantica expr1 = lerFator();
			ExpressaoSemantica expr2 = auxTermo();

			if (expr2 == null) {
				expr1.setVarTemp(expr1.getVarTemp());
				expr1.setOperador("*");
				return expr1;
			}

			// faz convers�o explicita para float
			if (expr1.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr2.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr2.setVarTemp(Gen.geraConversao(expr2.getVarTemp()));

			} else if (expr2.getTipo() == CodigosTokens.PALAVRA_FLOAT
					&& expr1.getTipo() == CodigosTokens.PALAVRA_INT) {

				expr1.setVarTemp(Gen.geraConversao(expr1.getVarTemp()));

			}

			int tipo = coercaoTipos(expr1.getTipo(), expr2.getTipo());
			String var = Gen.varTemp(expr1.getVarTemp(), expr2.getOperador()
					+ expr2.getVarTemp());
			return new ExpressaoSemantica(var, tipo, "*");
		}

		return null;
	}

	private ExpressaoSemantica lerFator() throws Exception {
		Token tk = token;
		ExpressaoSemantica expressao = new ExpressaoSemantica();

		if (validarToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES)) {
			expressao = lerExprAritmetica();
			if (!validarToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES)) {
				erroSintatico("Token ')' esperado.");
			}

		} else if (validarToken(ClasseToken.Identificador)) {
			if (!TabelaDeSimbolos.existeSimbolo(tk.getLexema())) {
				erroSemantico("Vari�vel " + tk.getLexema() + " n�o declarada.");
			}
			Simbolo s = TabelaDeSimbolos.getSimbolo(tk.getLexema(), escopo);
			expressao.setTipo(s.getTipo());
			expressao.setVarTemp(s.getNome());

		} else if (validarToken(ClasseToken.ConstanteChar)) {
			expressao.setTipo(CodigosTokens.PALAVRA_CHAR);
			expressao.setVarTemp(tk.getLexema());

		} else if (validarToken(ClasseToken.ConstanteFloat)) {
			expressao.setTipo(CodigosTokens.PALAVRA_FLOAT);
			expressao.setVarTemp(tk.getLexema());

		} else if (validarToken(ClasseToken.ConstanteInt)) {
			expressao.setTipo(CodigosTokens.PALAVRA_INT);
			expressao.setVarTemp(tk.getLexema());

		} else {
			erroSintatico("Uma constante float, char, int, um nome de vari�vel ou uma express�o aritm�tica esperada.");
		}
		return expressao;
	}

	// ler uma declara��o de vari�vel da linguagem
	private void lerDeclVariavel() throws Exception {
		// guarda o tipo da vari�vel
		int codigoTipo = token.getCodigoToken();

		// se n�o for int, char ou float, d� erro de compila��o
		if (!(validarToken(CodigosTokens.PALAVRA_CHAR)
				|| validarToken(CodigosTokens.PALAVRA_INT) || validarToken(CodigosTokens.PALAVRA_FLOAT))) {
			erroSintatico("Palavra chave int, float ou char esperado.");
		}

		// ler declara��o de variavel
		auxLerVariavel(codigoTipo);

		// ler todas as subdeclara��es da declara��o ex.: int x, y, z;
		while (validarToken(CodigosTokens.ESPECIAL_VIRGULA)) {
			auxLerVariavel(codigoTipo);
		}

		// se o ultimo caracter da declara��o n�o for ';' d� erro de
		// compila��o
		if (!validarToken(CodigosTokens.ESPECIAL_PONTO_E_VIRGULA))
			erroSintatico("Token ';' esperado.");
	}

	private void auxLerVariavel(int codigoTipo) throws Exception {
		Token auxToken = this.token;

		// pega o proximo token e verifica se ele � um identificador, caso
		// n�o for um identificador d� erro de compila��o.
		if (validarToken(ClasseToken.Identificador)) {

			// verifica se j� existe esta variavel
			if (TabelaDeSimbolos.existeSimbolo(auxToken.getLexema(), escopo)) {
				erroSemantico("Vari�vel " + auxToken.getLexema()
						+ " j� declarada.");
			}

			// sendo ele um identificador, adiciona-o na tabela de simbolos
			TabelaDeSimbolos.novoSimbolo(new Simbolo(codigoTipo, auxToken
					.getLexema(), escopo));
		} else
			erroSintatico("Identificador da vari�vel esperado.");
	}

	private int coercaoTipos(int tipo1, int tipo2)
			throws ErroSemanticoException {

		// verifica se � char
		if (tipo1 == CodigosTokens.PALAVRA_CHAR
				|| tipo2 == CodigosTokens.PALAVRA_CHAR) {
			erroSemantico("Opera��o n�o permitida para tipos caractere.");
		}
		// verifica se s�o compativeis
		if (tipo1 == CodigosTokens.PALAVRA_FLOAT
				|| tipo2 == CodigosTokens.PALAVRA_FLOAT) {

			return CodigosTokens.PALAVRA_FLOAT;
		}

		return CodigosTokens.PALAVRA_INT;
	}

	private void erroSintatico(String msg) throws ErroSintaticoException {
		if (token != null) {
			throw new ErroSintaticoException(token.getLinha(), token
					.getColuna(), msg);
		} else if (scan.fimDoArquivo()) {
			throw new ErroSintaticoException("no fim do arquivo.\n" + msg);
		} else {
			throw new ErroSintaticoException("n�o identificado.\n" + msg);
		}
	}

	private void erroSemantico(String msg) throws ErroSemanticoException {
		if (token != null) {
			throw new ErroSemanticoException(token.getLinha(), token
					.getColuna(), msg);
		} else {
			throw new ErroSemanticoException("n�o identificado.\n" + msg);
		}
	}
}
