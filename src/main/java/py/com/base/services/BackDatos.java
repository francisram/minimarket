package py.com.base.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import py.com.base.clases.ColumnInfo;
import py.com.base.clases.DtoGenerator;
import py.com.base.clases.TableMetadataRepository;
import py.com.base.utils.AppConfig;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;

@SpringBootConfiguration
@SpringBootApplication
@ComponentScan(basePackages = { "py.com.base.datasources", "py.com.base.clases", "py.com.base.services",
		"py.com.base.interfaces", "py.com.base.dao" })
@EntityScan(basePackages = "py.com.base.entities")
@EnableJpaRepositories(basePackages = "py.com.base.dao")
@EnableScheduling
public class BackDatos implements CommandLineRunner {

	@Autowired
	private TableMetadataRepository repository;

	@Autowired
	private ApplicationContext context;
	

	public static void main(String[] args) {
		SpringApplication.run(BackDatos.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		//py.com.bepsa.logs.LogUtil.crearSeq(AppConfig.SEQ_URL);
		LoggerUtil.importante( "Inicio de la aplicacion");
	//	py.com.bepsa.logs.LogUtil.closeAndFlush();
		crearCarpeta();
		DataSource as400DataSource = context.getBean("as400DataSource", DataSource.class);
		DataSource postgresDataSourceDestino = context.getBean("postgresDataSource2", DataSource.class);
		List<ColumnInfo> columns = repository.getTableColumnsOfas400("GXFINDTA", "VIEW_BUS_DATOS");
		List<ColumnInfo> columnsCeeAmq = repository.getTableColumnsOfas400("GXFINDTA", "CONFIGURACION_ENTIDAD_EMIADQ");
		DtoGenerator dtoGenerator = new DtoGenerator();
		dtoGenerator.generateDto("TBDAUV", columns, false);
		dtoGenerator.generateDto("configuracion_entidad_emiadq", columnsCeeAmq, true);
		try {
			for (int i = 0; i < AppConfig.MAXTHREADS; i++) {
				iniciarProcesamiento();
			}
		} catch (Exception ex) {
			String body = "Se produjo un error: " + ex.getMessage() + "\nDetalles:\n" + Arrays.toString(ex.getStackTrace());
			LogUtil.error(String.format("ERROR DB2 QUEUE PROCESSOR", body));
			ex.printStackTrace();
		}
		
	}
	
	public void iniciarProcesamiento() {
	    Thread thread = new Thread(() -> procesarDatosDelQueue());
	    thread.setDaemon(true); 
	    thread.start();
	}
	
	  private void procesarDatosDelQueue() {
	      
	        DataSource postgresDataSourceDestino = context.getBean("postgresDataSource2", DataSource.class);
	        DataQueueProcessor dq = new DataQueueProcessor(postgresDataSourceDestino);
	        try {
	        	dq.procesarDatosDelQueue();				
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	  
	  
	  public void crearCarpeta() {
		  File logDir = new File("./logs");
	        if (!logDir.exists()) {
	            boolean created = logDir.mkdirs();
	            if (created) {
	            	LogUtil.info("Carpeta 'logs' creada exitosamente.");
	            } else {
	            	LogUtil.info("No se pudo crear la carpeta 'logs'.   o ya existe");
	            }
	        }
	  }

}
