package br.unicap.comp;

import java.io.File;

import br.unicap.comp.parser.Parse;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// verifica se o parametro foi passado
		if (args.length != 1) {
			System.err.println("Arquivo fonte não especificado.");
			return;
		} else {
			// verifica se o fonte existe
			String fonte = args[0];
			File f = new File(fonte);
			if (!f.exists()) {
				System.err.println("Arquivo fonte não existe.");
				return;
			}

			// executa o parser
			try {
				// args[1]="src_comp.txt";
				Parse p = new Parse();
				p.executar(fonte);
			} catch (ErroCompilacaoException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
