/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The type Application config.
 */
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {


    private final String resourceBundlePath;
    private final AWSSettings awsSettings;


    /**
     * Instantiates a new Application config.
     *
     * @param resourceBundlePath the resource bundle path
     */
    public ApplicationConfig(@Value("${resource.bundle.path:classpath:i18n/language}") String resourceBundlePath,
                             AWSSettings awsSettings) {
        this.resourceBundlePath = resourceBundlePath;
        this.awsSettings = awsSettings;
    }

    /**
     * Object mapper object mapper.
     *
     * @return the object mapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    /**
     * Message source message source.
     *
     * @return the message source
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
        bean.setBasename(this.resourceBundlePath);
        bean.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return bean;
    }

    /**
     * Validator local validator factory bean.
     *
     * @return the local validator factory bean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean beanValidatorFactory = new LocalValidatorFactoryBean();
        beanValidatorFactory.setValidationMessageSource(this.messageSource());
        beanValidatorFactory.setMessageInterpolator(new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(this.messageSource())));
        return beanValidatorFactory;
    }


    /**
     * Locale resolver locale resolver.
     *
     * @return the locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    @Nullable
    @Override
    public Validator getValidator() {
        return this.validator();
    }

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard().
                withRegion(this.awsSettings.region()).
                withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return ApplicationConfig.this.awsSettings.accessKey();
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return ApplicationConfig.this.awsSettings.secretKey();
                            }
                        };
                    }

                    @Override
                    public void refresh() {
                        //Do nothing
                    }
                }).build();
    }

    @Bean
    public AmazonRoute53 amazonRoute53() {
        return AmazonRoute53ClientBuilder.standard().withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return ApplicationConfig.this.awsSettings.accessKey();
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return ApplicationConfig.this.awsSettings.secretKey();
                            }
                        };
                    }

                    @Override
                    public void refresh() {
                        //Do nothing
                    }
                })
                .withRegion(this.awsSettings.region())
                .build();
    }

    @Bean
    public SpecificationUtil specificationUtil() {
        return new SpecificationUtil();
    }
}
