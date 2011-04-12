package br.unicap.comp;

public class Token {
	private StringBuilder sb;
	private ClasseToken classe;
	private int codigoToken;
	private int linha;
	private int coluna;

	public Token(int linha, int coluna) {
		this.sb = new StringBuilder();
		this.linha = linha;
		this.coluna = coluna;
		this.codigoToken = -1;
	}

	public void setLinha(int linha) {
		this.linha = linha;
	}

	public void setColuna(int coluna) {
		this.coluna = coluna;
	}

	public int getLinha() {
		return linha;
	}

	public int getColuna() {
		return coluna;
	}

	public int getCodigoToken() {
		return codigoToken;
	}

	public void setCodigoToken(int codigoToken) {
		this.codigoToken = codigoToken;
	}

	public String getLexema() {
		return sb.toString();
	}

	public void appendChar(char ch) {
		this.sb.append(ch);
	}

	public void limparToken() {
		sb.delete(0, sb.length());
	}

	public ClasseToken getClasseDoToken() {
		return classe;
	}

	public void setClasseDoToken(ClasseToken classe) {
		this.classe = classe;
	}
}
