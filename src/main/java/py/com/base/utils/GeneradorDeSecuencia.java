package py.com.base.utils;

public class GeneradorDeSecuencia {
	
	public static int contador;

    // Constructor que inicializa el contador en un valor inicial (por ejemplo, 1)
    public GeneradorDeSecuencia(int valorInicial) {
        contador = valorInicial;
    }

    // Metodo para obtener el siguiente valor de la secuencia
    public static  int obtenerSiguiente() {
        return contador++;
    }

    // Metodo para reiniciar el contador a un valor especifico si es necesario
    public void reiniciar(int nuevoValor) {
        contador = nuevoValor;
    }

}
