package br.unicap.comp;

public class ErroCompilacaoException extends Exception {
	private int linha;
	private int coluna;

	public ErroCompilacaoException(int linha, int coluna, String msg) {
		super(msg);
		this.linha = linha;
		this.coluna = coluna;
	}
}
