package br.unicap.comp.parser;

import br.unicap.comp.ErroCompilacaoException;

public class ErroSintaticoException extends ErroCompilacaoException {
	public ErroSintaticoException(int linha, int coluna, String msg) {
		super(linha, coluna, String.format(
				"[Erro sint�tico] linha: %d, coluna: %d\n%s", linha, coluna,
				msg));
	}

	public ErroSintaticoException(String msg) {
		super(-1, -1, "[Erro sint�tico] " + msg);
	}
}
