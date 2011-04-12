package br.unicap.comp.parser;

public class ExpressaoSemantica {

	public ExpressaoSemantica() {

	}

	public ExpressaoSemantica(String var, int tipo) {
		this.tipo = tipo;
		this.varTemp = var;
	}

	public ExpressaoSemantica(String var, int tipo, String operador) {
		this.tipo = tipo;
		this.varTemp = var;
		this.opera = operador;
	}
	
	private int tipo;
	private String opera;
	private String varTemp;

	
	public String getOperador(){
		return opera;
	}
	
	public void setOperador(String operador){
		this.opera = operador;
	}
	
	public String getVarTemp() {
		return varTemp;
	}

	public void setVarTemp(String var) {
		this.varTemp = var;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
}
