package br.unicap.comp.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.unicap.comp.ClasseToken;
import br.unicap.comp.CodigosTokens;
import br.unicap.comp.DicionarioPalavrasReservadas;
import br.unicap.comp.Token;

public class Scan {
	private FileInputStream fonte;
	private String arquivo;
	private char lookAhead;
	private int posicao;
	private int coluna;
	private int linha;
	private boolean fimArquivo;

	public Scan(String fonte) throws FileNotFoundException {
		this.arquivo = fonte;
		this.posicao = 0;
		this.lookAhead = '\0';
		this.coluna = 1;
		this.linha = 1;
		this.fimArquivo = false;
	}

	private void lerChar() throws IOException {
		// lê o proximo caracter e incrementa a posição
		int aux = this.fonte.read();
		this.posicao++;
		if (aux == -1) {
			this.fimArquivo = true;
			this.lookAhead = '\0';
		} else {
			this.lookAhead = (char) aux;
			this.coluna++;
		}
	}

	private void lerProximoChar(Token token) throws IOException {
		// monta o token
		token.appendChar(this.lookAhead);

		// ler o proximo char
		this.lerChar();

		// atualiza linha e coluna
		//token.setColuna(this.coluna);
		//token.setLinha(this.linha);
	}

	private void abreArquivo() throws IOException {
		this.fonte = new FileInputStream(arquivo);
		this.fonte.skip(posicao);
	}

	private void fechaArquivo() throws IOException {
		if (this.fonte != null) {
			this.fonte.close();
		}
	}

	public boolean fimDoArquivo() {
		return this.fimArquivo;
	}

	public Token proximoToken() throws Exception {
		try {
			// abre o arquivo já na posição correta
			this.abreArquivo();
			while (!this.fimArquivo) {

				// cria o token setanto a linha e a coluna onde ele está
				Token token = new Token(this.linha, this.coluna);

				// lê o primeiro caractere, sse, o arquivo está sendo aberto
				// pela primeira vez
				if (this.posicao == 0) {
					this.lerChar();
				}

				// caso for uma quebra de linha, ignora o que foi lido, lê
				// novamente e incrementa o contador de linha
				if (Character.isWhitespace(this.lookAhead)) {
					trataBrancos();
					this.lerChar();
					continue;
				}

				/*
				 * IDENTIFICADOR OU PALAVRA RESERVADA
				 */
				if (Character.isLetter(this.lookAhead)) {

					do {
						this.lerProximoChar(token);
					} while (Character.isLetterOrDigit(lookAhead));

					// se o token montado existir no dicionario, então ele é uma
					// palavra reservada
					int auxCodigo = DicionarioPalavrasReservadas
							.getCodigo(token.getLexema());
					if (auxCodigo > -1) {
						token.setClasseDoToken(ClasseToken.PalavraReservada);
						token.setCodigoToken(auxCodigo);
						token.limparToken();
					} else {
						token.setClasseDoToken(ClasseToken.Identificador);
					}

					return token;
				}
				/*
				 * CONSTANTE INT OU FLOAT
				 */
				else if (Character.isDigit(this.lookAhead)) {

					// se for um digito seta o token para inteiro
					// temporariamente
					token.setClasseDoToken(ClasseToken.ConstanteInt);
					do {
						this.lerProximoChar(token);
					} while (Character.isDigit(this.lookAhead));

					// se for um ponto então seta o token para float
					// temporariamente
					if (this.lookAhead == '.') {
						token.setClasseDoToken(ClasseToken.ConstanteFloat);
						this.lerProximoChar(token);

						// caso depois do ponto não for um digito, então existe
						// um erro léxico
						if (!Character.isDigit(this.lookAhead))
							throw new ErroLexicoException(token.getLinha(),
									token.getColuna(),
									"Constante float mal formada.");
						do {
							this.lerProximoChar(token);
						} while (Character.isDigit(this.lookAhead));
					}

					return token;
				}
				/*
				 * CONSTANTE FLOAT OU CARACTER PONTO
				 */
				else if (this.lookAhead == '.') {

					this.lerProximoChar(token);

					// se não for um digito seta o token para caracter especial
					// "." (ponto)
					if (!Character.isDigit(this.lookAhead)) {
						token.setClasseDoToken(ClasseToken.CaracterEspecial);
						token.setCodigoToken(CodigosTokens.ESPECIAL_PONTO);
						token.limparToken();
					} else {
						// se não for caracter ponto, então é uma constante
						// float
						token.setClasseDoToken(ClasseToken.ConstanteFloat);
						do {
							this.lerProximoChar(token);
						} while (Character.isDigit(lookAhead));
					}

					return token;
				}
				/*
				 * CARACTER ESPECIAL , (virgula)
				 */
				else if (this.lookAhead == ',') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token.setCodigoToken(CodigosTokens.ESPECIAL_VIRGULA);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL ; (ponto-e-virgula)
				 */
				else if (this.lookAhead == ';') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token
							.setCodigoToken(CodigosTokens.ESPECIAL_PONTO_E_VIRGULA);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL ( (abre parêntese)
				 */
				else if (this.lookAhead == '(') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token
							.setCodigoToken(CodigosTokens.ESPECIAL_ABRE_PARENTESES);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL ) (fecha parêntese)
				 */
				else if (this.lookAhead == ')') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token
							.setCodigoToken(CodigosTokens.ESPECIAL_FECHA_PARENTESES);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL { (abre chaves)
				 */
				else if (this.lookAhead == '{') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token.setCodigoToken(CodigosTokens.ESPECIAL_ABRE_CHAVES);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL } (fecha chaves)
				 */
				else if (this.lookAhead == '}') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.CaracterEspecial);
					token.setCodigoToken(CodigosTokens.ESPECIAL_FECHA_CHAVES);
					token.limparToken();
					return token;
				}
				/*
				 * CARACTER ESPECIAL ' (aspa) OU CONSTANTE CHAR
				 */
				else if (this.lookAhead == '\'') {
					this.lerProximoChar(token);

					if (Character.isLetterOrDigit(this.lookAhead)) {
						this.lerProximoChar(token);
						if (this.lookAhead == '\'') {
							token.setClasseDoToken(ClasseToken.ConstanteChar);
							this.lerProximoChar(token);
						} else {
							throw new ErroLexicoException(token.getLinha(),
									token.getColuna(),
									"Constante char mal formada.");
						}
					} else {
						token.setClasseDoToken(ClasseToken.CaracterEspecial);
						token.setCodigoToken(CodigosTokens.ESPECIAL_ASPAS);
						token.limparToken();
					}
					return token;
				}
				/*
				 * OPERADOR RELACIONAL < (menor que) OU <= (menor ou igual)
				 */
				else if (this.lookAhead == '<') {
					token.setClasseDoToken(ClasseToken.OperadorRelacional);

					this.lerProximoChar(token);

					if (this.lookAhead == '=') {
						this.lerProximoChar(token);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_MENOR_IGUAL);
						token.limparToken();
					} else {
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_MENOR);
						token.limparToken();
					}
					return token;
				}
				/*
				 * OPERADOR RELACIONAL > (maior que) OU >= (maior ou igual)
				 */
				else if (this.lookAhead == '>') {
					token.setClasseDoToken(ClasseToken.OperadorRelacional);

					this.lerProximoChar(token);

					if (this.lookAhead == '=') {
						this.lerProximoChar(token);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_MAIOR_IGUAL);
						token.limparToken();

					} else {
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_MAIOR);
						token.limparToken();
					}

					return token;
				}
				/*
				 * OPERADOR ARITIMÉTICO = (igual) OU OPERADOR RELACIONAL ==
				 * (igual igual)
				 */
				else if (this.lookAhead == '=') {
					this.lerProximoChar(token);

					if (this.lookAhead == '=') {
						this.lerProximoChar(token);

						token.setClasseDoToken(ClasseToken.OperadorRelacional);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_IGUAL);
						token.limparToken();
					} else {
						token.setClasseDoToken(ClasseToken.OperadorAritimetico);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_ARIT_ATRIBUICAO);
						token.limparToken();
					}

					return token;
				}
				/*
				 * OPERADOR RELACIONAL != (diferente)
				 */
				else if (this.lookAhead == '!') {
					this.lerProximoChar(token);

					if (this.lookAhead == '=') {
						this.lerProximoChar(token);

						token.setClasseDoToken(ClasseToken.OperadorRelacional);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_RELAC_DIFERENTE);
						token.limparToken();

					} else {
						throw new ErroLexicoException(token.getLinha(), token
								.getColuna(),
								"Operador relacional inválido, esperado \"!=\".");
					}

					return token;
				}
				/*
				 * OPERADOR ARITIMÉTICO + (adição)
				 */
				else if (this.lookAhead == '+') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.OperadorAritimetico);
					token.setCodigoToken(CodigosTokens.OPERADOR_ARIT_ADICAO);
					token.limparToken();

					return token;
				}
				/*
				 * OPERADOR ARITIMÉTICO - (subtração)
				 */
				else if (this.lookAhead == '-') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.OperadorAritimetico);
					token.setCodigoToken(CodigosTokens.OPERADOR_ARIT_SUBTRACAO);
					token.limparToken();

					return token;
				}
				/*
				 * OPERADOR ARITIMÉTICO * (multiplicação)
				 */
				else if (this.lookAhead == '*') {
					this.lerProximoChar(token);

					token.setClasseDoToken(ClasseToken.OperadorAritimetico);
					token
							.setCodigoToken(CodigosTokens.OPERADOR_ARIT_MULTIPLICACAO);
					token.limparToken();

					return token;
				}
				/*
				 * OPERADOR ARITIMÉTICO / (divisão)
				 */
				else if (this.lookAhead == '/') {
					this.lerProximoChar(token);

					if (this.lookAhead == '*') {
						return lerComentario(token);
					} else {
						token.setClasseDoToken(ClasseToken.OperadorAritimetico);
						token
								.setCodigoToken(CodigosTokens.OPERADOR_ARIT_DIVISAO);
						token.limparToken();
						return token;
					}
				}
				/*
				 * CARACTER NÃO RECONHECIDO PELA LINGUAGEM
				 */
				else {
					throw new ErroLexicoException(linha, coluna,
							"Caractere especificado não é reconhecido pela linguagem.");
				}
			}
			return null;
		} finally {
			fechaArquivo();
		}
	}

	private void trataBrancos() {
		if (this.lookAhead == '\n') {
			this.linha++;
			this.coluna = 0;
		} else if (this.lookAhead == '\t'){
			this.coluna += 3;
		}
	}

	private Token lerComentario(Token token) throws Exception {
		do {
			if (Character.isWhitespace(this.lookAhead)) {
				trataBrancos();
			}
			this.lerProximoChar(token);
		} while (this.lookAhead != '*' && !this.fimArquivo);

		if (this.fimArquivo) {
			throw new ErroLexicoException(token.getLinha(), token.getColuna(),
					"Fim do comentário esperado.");
		}

		do {
			this.lerProximoChar(token);
		} while (this.lookAhead == '*');

		if (this.fimArquivo) {
			throw new ErroLexicoException(token.getLinha(), token.getColuna(),
					"Fim do comentário esperado.");
		}

		if (this.lookAhead != '/') {
			return this.lerComentario(token);
		} else {
			this.lerProximoChar(token);
			token.limparToken();
			return this.proximoToken();
		}
	}
}
