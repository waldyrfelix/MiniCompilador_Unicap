package br.unicap.comp.codegen;

import br.unicap.comp.CodigosTokens;
import br.unicap.comp.DicionarioPalavrasReservadas;

public class Gen {
	private static int contLabel;
	private static int contTemp;

	private static void gerar(String str) {
		System.out.println(str);
	}

	public static String geraLabel(boolean escreve) {
		String aux = "L" + contLabel;
		if (escreve)
			gerar(aux + ":");
		contLabel++;
		return aux;
	}

	public static String varTemp(String expr1, String expr2) {
		String temp = "t" + contTemp;
		gerar(String.format(" %-3s= %s%s", temp, expr1, expr2));
		contTemp++;
		return temp;
	}

	public static String atrib(String var, String expr) {
		gerar(String.format(" %-3s= %s", var, expr));
		return var;
	}

	public static String geraIf(String expr) {
		String l1 = geraLabel(false);
		geraIf(expr, null, l1);
		return l1;
	}
	
	public static void geraIf(String expr, String label) {
		geraIf(expr, null, label);
	}
	
	public static void geraIf(String expr, String operador, String label) {
		if (operador != null) {
			if (operador.equals(">=")) {
				expr = expr.replaceFirst(">=", "<");
				
			} else if (operador.equals("<=")) {
				expr = expr.replaceFirst("<=", ">");
				
			} else if (operador.equals("==")) {
				expr = expr.replaceFirst("==", "!=");
				
			} else if (operador.equals("!=")) {
				expr = expr.replaceFirst("!=", "==");
				
			} else if (operador.equals(">")) {
				expr = expr.replaceFirst(">", "<=");
				
			} else if (expr.contains("<")) {
				expr = expr.replaceFirst("<", ">=");
			}
		}
		gerar(" if " + expr + " goto " + label);
	}

	public static void geraGoto(String label) {
		gerar(" goto " + label);
	}

	public static void geraLabel(String label) {
		gerar(label + ":");
	}

	public static String geraConversao(String var) {
		return varTemp("(float)", var);
	}
}
