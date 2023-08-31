package eu.gaiax.wizard;

import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The type Wizard application.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableFeignClients
@ConfigurationPropertiesScan
public class GaiaXWizardApplication {

    /**
     * The entry point of Wizard application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GaiaXWizardApplication.class, args);
    }

    @Bean
    public SpecificationUtil specificationUtil() {
        return new SpecificationUtil();
    }
}