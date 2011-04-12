package br.unicap.comp.parser;

public class Simbolo {
	private int tipo;
	private String nome;
	private int escopo;

	public Simbolo(int tipo, String nome, int escopo) {
		this.tipo = tipo;
		this.nome = nome;
		this.escopo = escopo;
	}

	public int getEscopo() {
		return escopo;
	}

	public void setEscopo(int escopo) {
		this.escopo = escopo;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

}
