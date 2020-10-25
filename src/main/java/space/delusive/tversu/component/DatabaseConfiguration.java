package space.delusive.tversu.component;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("space.delusive.tversu.repository")
public class DatabaseConfiguration {
    public static final String PACKAGES_TO_SCAN = "space.delusive.tversu.entity";

    @Bean
    DriverManagerDataSource dataSource(@Value("${db.driver}") String driverClassName,
                                       @Value("${db.url}") String url,
                                       @Value("${db.username}") String username,
                                       @Value("${db.password}") String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                @Value("${hibernate.dialect}") String hibernateDialect,
                                                                @Value("${hibernate.auto.generate}") String hibernateAutoGenerate,
                                                                @Value("${hibernate.show.sql}") String showSql) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateAutoGenerate);
        properties.setProperty("hibernate.globally_quoted_identifiers", "true");
        properties.setProperty("hibernate.show_sql", showSql);
        properties.setProperty("hibernate.format_sql", showSql);
        properties.setProperty("hibernate.use_sql_comments", showSql);
        entityManagerFactory.setJpaProperties(properties);
        entityManagerFactory.setPackagesToScan(PACKAGES_TO_SCAN);
        return entityManagerFactory;
    }

    @Bean
    JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        return transactionManager;
    }
}
