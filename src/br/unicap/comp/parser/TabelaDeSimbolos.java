package br.unicap.comp.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;

public class TabelaDeSimbolos {
	private static ArrayList<Simbolo> tabela;

	private static class CompSimbolos implements Comparator<Simbolo> {
		@Override
		public int compare(Simbolo o1, Simbolo o2) {
			if (o1.getEscopo() < o2.getEscopo())
				return 1;
			else if (o1.getEscopo() > o2.getEscopo())
				return -1;
			else
				return 0;
		}
	}

	static {
		if (tabela == null)
			tabela = new ArrayList<Simbolo>();
	}

	public static void novoSimbolo(Simbolo simb) {
		tabela.add(simb);
		Collections.sort(tabela, new CompSimbolos());
	}

	public static boolean existeSimbolo(String nome, int escopo) {
		for (Simbolo simb : tabela) {
			if (simb.getNome().equals(nome) && simb.getEscopo() == escopo)
				return true;
		}
		return false;
	}

	public static boolean existeSimbolo(String nome) {
		Iterator<Simbolo> it = tabela.iterator();
		while (it.hasNext()) {
			Simbolo simb = it.next();
			if (nome.equals(simb.getNome()))
				return true;
		}
		return false;
	}

	public static Simbolo getSimbolo(String nome, int escopo) {
		for (Simbolo simb : tabela) {
			if (simb.getNome().equals(nome) && simb.getEscopo() == escopo)
				return simb;
		}
		return getSimbolo(nome);
	}

	public static Simbolo getSimbolo(String nome) {
		for (Simbolo simb : tabela) {
			if (simb.getNome().equals(nome))
				return simb;
		}
		return null;
	}

	public static void excluirSimbolos(int escopo) {
		ArrayList<Simbolo> lista = new ArrayList<Simbolo>();
		for (Simbolo simb : tabela) {
			if (simb.getEscopo() == escopo)
				lista.add(simb);
		}
		tabela.removeAll(lista);
		Collections.sort(tabela, new CompSimbolos());
	}
}
