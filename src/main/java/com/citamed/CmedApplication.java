package com.citamed;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CmedApplication {

    // Inicia la aplicación web de CitaMed.
    public static void main(String[] args) {
        // Carga las variables de .env (si existe) como propiedades del sistema,
        // para que application.properties las pueda leer sin necesidad de exportarlas
        // manualmente en el sistema operativo. Si no hay .env, no pasa nada.
        Dotenv.configure().ignoreIfMissing().load().entries()
                .forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
        SpringApplication.run(CmedApplication.class, args);
    }

}
