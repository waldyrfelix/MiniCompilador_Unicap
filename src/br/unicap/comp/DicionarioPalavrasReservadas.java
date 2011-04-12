package br.unicap.comp;

import java.util.HashMap;
import java.util.Map.Entry;

public class DicionarioPalavrasReservadas {
	private static HashMap<Integer, String> dicionario;
	static {
		dicionario = new HashMap<Integer, String>();
		dicionario.put(CodigosTokens.PALAVRA_MAIN, "main");
		dicionario.put(CodigosTokens.PALAVRA_IF, "if");
		dicionario.put(CodigosTokens.PALAVRA_ELSE, "else");
		dicionario.put(CodigosTokens.PALAVRA_WHILE, "while");
		dicionario.put(CodigosTokens.PALAVRA_DO, "do");
		dicionario.put(CodigosTokens.PALAVRA_FOR, "for");
		dicionario.put(CodigosTokens.PALAVRA_INT, "int");
		dicionario.put(CodigosTokens.PALAVRA_FLOAT, "float");
		dicionario.put(CodigosTokens.PALAVRA_CHAR, "char");

		dicionario.put(CodigosTokens.OPERADOR_ARIT_ADICAO, "+");
		dicionario.put(CodigosTokens.OPERADOR_ARIT_SUBTRACAO, "-");
		dicionario.put(CodigosTokens.OPERADOR_ARIT_DIVISAO, "/");
		dicionario.put(CodigosTokens.OPERADOR_ARIT_MULTIPLICACAO, "*");
		dicionario.put(CodigosTokens.OPERADOR_ARIT_ATRIBUICAO, "=");

		dicionario.put(CodigosTokens.OPERADOR_RELAC_IGUAL, "==");
		dicionario.put(CodigosTokens.OPERADOR_RELAC_DIFERENTE, "!=");
		dicionario.put(CodigosTokens.OPERADOR_RELAC_MAIOR, ">");
		dicionario.put(CodigosTokens.OPERADOR_RELAC_MAIOR_IGUAL, ">=");
		dicionario.put(CodigosTokens.OPERADOR_RELAC_MENOR, "<");
		dicionario.put(CodigosTokens.OPERADOR_RELAC_MENOR_IGUAL, "<=");
	}

	public static int getCodigo(String token) {
		for (Entry<Integer, String> item : dicionario.entrySet()) {
			if (item.getValue().equals(token))
				return item.getKey();
		}
		return -1;
	}

	public static String getPalavra(int codigo) {
		return dicionario.get(codigo);
	}
}
