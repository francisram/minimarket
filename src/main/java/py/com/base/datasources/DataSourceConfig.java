package py.com.base.datasources;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariDataSource;
import py.com.base.utils.AppConfig;


/**
 * Configura las fuentes de datos (AS400 y PostgreSQL) y los JdbcTemplate
 * correspondientes.
 */

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
public class DataSourceConfig {

	//private HikariDataSource as400DataSource;
	private HikariDataSource postgresDataSource2;

	/*
	@Bean
	@Primary
	@ConfigurationProperties(prefix = "app.datasource.as400")
	public DataSourceProperties as400DataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "as400DataSource")
	@Primary
	public HikariDataSource as400DataSource(@Qualifier("as400DataSourceProperties") DataSourceProperties properties)
			throws Throwable {
		as400DataSource = new HikariDataSource();
		as400DataSource.setJdbcUrl(properties.getUrl());
		as400DataSource.setUsername(AppConfig.ENCRYPTEDUSERNAMEAS400.trim());
		as400DataSource.setPassword(AppConfig.ENCRYPTEDPASSWORDAS400.trim());
		as400DataSource.setMaximumPoolSize(AppConfig.MAXPULLSIZEAS400);
		as400DataSource.setIdleTimeout(AppConfig.TIMEOUTAS400);
		as400DataSource.setConnectionTimeout(AppConfig.REQUEST_TIMEOUT_AS400);
		as400DataSource.setMaxLifetime(AppConfig.MAX_LIFE_AS400);
		as400DataSource.setDriverClassName(properties.getDriverClassName());
		return as400DataSource;
	}

	@Bean(name = "as400JdbcTemplate")
	public JdbcTemplate as400JdbcTemplate(@Qualifier("as400DataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	*/

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "app.datasource.postgres2")
	public DataSourceProperties postgresDataSourceDestinoProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "postgresDataSource2")
	@Primary
	public HikariDataSource postgresDataSource2(
			@Qualifier("postgresDataSourceDestinoProperties") DataSourceProperties postgresDataSourceDestinoProperties)
			throws Throwable {
		postgresDataSource2 = new HikariDataSource();
		postgresDataSource2.setJdbcUrl(postgresDataSourceDestinoProperties.getUrl());
		postgresDataSource2.setUsername(AppConfig.ENCRYPTEDUSERNAMEPOSTGRES2);
		postgresDataSource2.setPassword(AppConfig.ENCRYPTEDPASSWORDPOSTGRES2);
		postgresDataSource2.setDriverClassName(postgresDataSourceDestinoProperties.getDriverClassName());
		postgresDataSource2.setMaximumPoolSize(AppConfig.MAXPULLSIZEPOSTGRES);
		postgresDataSource2.setIdleTimeout(AppConfig.TIMEOUTPOSTGRES);
		postgresDataSource2.setConnectionTimeout(AppConfig.REQUEST_TIMEOUT_POSTGRES);
		postgresDataSource2.setMaxLifetime(AppConfig.MAX_LIFE_POSTGRES);

		return postgresDataSource2;
	}

	@Bean(name = "postgresJdbcTemplate2")
	public JdbcTemplate postgresJdbcTemplate2(@Qualifier("postgresDataSource2") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@PreDestroy
	public void closeDataSources() {
		/*
		if (as400DataSource != null) {
			as400DataSource.close();
			System.out.println(" HikariCP AS400 cerrado correctamente.");
		}
		*/
		if (postgresDataSource2 != null) {
			postgresDataSource2.close();
			System.out.println(" HikariCP PostgreSQL cerrado correctamente.");
		}
	}
}
