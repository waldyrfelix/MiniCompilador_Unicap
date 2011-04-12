package br.unicap.comp.parser;

import br.unicap.comp.ErroCompilacaoException;

public class ErroSemanticoException extends ErroCompilacaoException {
	public ErroSemanticoException(int linha, int coluna, String msg) {
		super(linha, coluna, String.format(
				"[Erro semântico] linha: %d, coluna: %d\n%s", linha, coluna,
				msg));
	}

	public ErroSemanticoException(String msg) {
		super(-1, -1, "[Erro semântico] " + msg);
	}
}
