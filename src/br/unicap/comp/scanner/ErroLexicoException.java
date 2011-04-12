package br.unicap.comp.scanner;

import br.unicap.comp.ErroCompilacaoException;

public class ErroLexicoException extends ErroCompilacaoException {

	public ErroLexicoException(int linha, int coluna, String msg) {
		super(linha, coluna, 
				String.format("[Erro léxico] linha: %d, coluna: %d\n%s", 
				linha, coluna, msg));
	}
	
}
